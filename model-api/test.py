import joblib
import pandas as pd
import numpy as np

# Load model
model = joblib.load("model.pkl")

# Check classes
print("Model classes:", model.classes_)
# Should print: [0 1]
# Meaning: class 0 = ABANDON, class 1 = TERMINE

# Create test sample
test_data = {
    'start_hour': 14,
    'start_dayofweek': 2,
    'deadline_gap_minutes': 5760,  # 4 days
    'is_night': 0,
    'is_weekend': 0,
    'last_5_tasks_abandonment_rate': 0.4,
    'last_7_days_completion_rate': 0.6,
    'deadline_pressure': 0.0001736,
    'high_pauses': 0,
    'bad_timing': 0,
    'very_bad_timing': 0,
    'hour_mismatch': 0,
    'hour_distance': 0,
    'workload_pressure': 0.0208,
    'user_stability': 0.7,
    'recent_drop_risk': 0.4,
}

# Add dummy one-hot encoded columns (adjust based on your training data)
test_df = pd.DataFrame([test_data])

# Make prediction
proba = model.predict_proba(test_df)[0]

print("\n📊 Probabilities:")
print(f"proba[0] (ABANDON): {proba[0]:.4f}")
print(f"proba[1] (COMPLETE): {proba[1]:.4f}")

print("\n✅ Interpretation:")
print(f"Risk of abandonment: {proba[0]*100:.1f}%")
print(f"Chance of completion: {proba[1]*100:.1f}%")