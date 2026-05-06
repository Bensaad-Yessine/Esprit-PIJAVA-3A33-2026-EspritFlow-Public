from flask import Flask, request, jsonify
from flask_cors import CORS
import joblib
import pandas as pd
import numpy as np

app = Flask(__name__)
CORS(app)

# Load model + features
model = joblib.load("model.pkl")
feature_columns = joblib.load("features.pkl")

def build_features_from_json(data):
    """
    Build features matching the EXACT training pipeline.
    """
    
    df = pd.DataFrame([data])

    # =========================================
    # 1. BASIC TIME FEATURES
    # =========================================
    df['date_debut'] = pd.to_datetime(df['date_debut'])
    df['date_fin'] = pd.to_datetime(df['date_fin'])

    df['start_hour'] = df['date_debut'].dt.hour
    df['start_dayofweek'] = df['date_debut'].dt.dayofweek
    df['deadline_gap_minutes'] = (df['date_fin'] - df['date_debut']).dt.total_seconds() / 60

    df['is_night'] = (df['start_hour'] < 6).astype(int)
    df['is_weekend'] = (df['start_dayofweek'] >= 5).astype(int)

    # =========================================
    # 2. USER CONTEXT FEATURES
    # =========================================
    df['last_5_tasks_abandonment_rate'] = data.get("last_5_tasks_abandonment_rate", 0.0)
    df['last_7_days_completion_rate'] = data.get("last_7_days_completion_rate", 
                                                   data.get("completion_rate", 0.5))

    # =========================================
    # 3. SOFT RULE FEATURES
    # =========================================
    df['deadline_pressure'] = 1 / (df['deadline_gap_minutes'] + 1)
    
    pause_freq = data.get("pause_frequency", 0)
    pause_q75 = data.get("pause_frequency_threshold", 4.0)
    df['high_pauses'] = np.array([pause_freq > pause_q75]).astype(int)[0]    
    df['bad_timing'] = ((df['is_night'] == 1) | (df['is_weekend'] == 1)).astype(int)
    df['very_bad_timing'] = ((df['is_night'] == 1) & (df['is_weekend'] == 1)).astype(int)
    
    most_productive_hour = data.get("most_productive_hour", df['start_hour'].iloc[0])
    df['hour_mismatch'] = (df['start_hour'] != most_productive_hour).astype(int)
    df['hour_distance'] = abs(df['start_hour'] - most_productive_hour)
    
    avg_duration = data.get("average_completion_duration_minutes", 100)
    df['workload_pressure'] = avg_duration / (df['deadline_gap_minutes'] + 1)
    
    df['user_stability'] = 1 - data.get("abandonment_rate", 0.25)
    df['recent_drop_risk'] = 1 - df['last_7_days_completion_rate']

    # =========================================
    # 4. FINAL FEATURE VECTOR
    # =========================================
    categorical = ['type', 'priorite']

    feature_cols = [
        'start_hour', 'start_dayofweek', 'deadline_gap_minutes',
        'is_night', 'is_weekend',
        'last_5_tasks_abandonment_rate',
        'last_7_days_completion_rate',
        'deadline_pressure',
        'high_pauses',
        'bad_timing',
        'very_bad_timing',
        'hour_mismatch',
        'hour_distance',
        'workload_pressure',
        'user_stability',
        'recent_drop_risk'
    ]

    df_model = df[feature_cols + categorical].copy()

    df_model = pd.get_dummies(df_model, columns=categorical, drop_first=True)
    df_model = df_model.replace([np.inf, -np.inf], np.nan).fillna(0)

    return df_model


@app.route("/predict", methods=["POST"])
def predict():
    try:
        data = request.json
        
        # Validate required fields
        required = ["date_debut", "date_fin", "type", "priorite"]
        for field in required:
            if field not in data:
                return jsonify({"error": f"Missing field: {field}"}), 400
        
        # Build features
        X_input = build_features_from_json(data)
        
        # Align with training columns
        X_input = X_input.reindex(columns=feature_columns, fill_value=0)
        
        # Get probabilities
        proba = model.predict_proba(X_input)[0]
        
        # 🔍 VERIFICATION: Print to console
        print(f"📊 Prediction probabilities: {proba}")
        print(f"   → Abandon (class 0): {proba[0]:.4f}")
        print(f"   → Complete (class 1): {proba[1]:.4f}")
        
        # Class prediction (1 if probability of completion > 0.5)
        prediction = int(proba[1] > 0.5)
        
        # Determine risk level based on abandon probability
        risk_level = "HIGH" if proba[0] > 0.7 else "MEDIUM" if proba[0] > 0.4 else "LOW"
        
        response = {
            "prediction": prediction,              # 0 = will abandon, 1 = will complete
            "probability_complete": float(proba[1]),  # Probability of TERMINE
            "probability_abandon": float(proba[0]),   # Probability of ABANDON
            "risk_level": risk_level
        }
        
        print(f"📤 Sending response: {response}")
        
        return jsonify(response)
    
    except Exception as e:
        print(f"❌ Error: {str(e)}")
        import traceback
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500


@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "ok", "model_loaded": model is not None})


@app.route("/test", methods=["GET"])
def test_prediction():
    """
    Test endpoint to verify class mapping
    """
    # Create a dummy sample
    sample_data = {
        "date_debut": "2024-05-01T09:00:00",
        "date_fin": "2024-05-05T17:00:00",
        "type": "ETUDE",
        "priorite": "HAUTE",
        "completion_rate": 0.6,
        "abandonment_rate": 0.4,
        "pause_frequency": 2.0,
        "most_productive_hour": 14,
        "last_5_tasks_abandonment_rate": 0.3,
        "last_7_days_completion_rate": 0.7
    }
    
    X_input = build_features_from_json(sample_data)
    X_input = X_input.reindex(columns=feature_columns, fill_value=0)
    
    proba = model.predict_proba(X_input)[0]
    
    return jsonify({
        "test_sample": sample_data,
        "probabilities": {
            "class_0_ABANDON": float(proba[0]),
            "class_1_COMPLETE": float(proba[1])
        },
        "interpretation": {
            "abandon_probability": f"{proba[0]*100:.1f}%",
            "complete_probability": f"{proba[1]*100:.1f}%",
            "prediction": "ABANDON" if proba[0] > 0.5 else "COMPLETE"
        }
    })


if __name__ == "__main__":
    app.run(debug=True, host='0.0.0.0', port=5000)