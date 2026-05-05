from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Optional
import uvicorn

app = FastAPI(title="Smart Room Recommendation API")

class Seance(BaseModel):
    jour_semaine: int
    heure_debut: int
    duree_min: int
    type_seance: str
    mode: str
    matiere: str
    groupe: str
    salle_capacite_initiale: int
    moy_presence_groupe_30j: float
    taux_absence_groupe_30j: float
    presence_moyenne_matiere: float
    presence_moyenne_creneau: float

class Room(BaseModel):
    id: str
    bloc: str
    etage: int
    capacite: int
    is_available: bool

class RecommendationRequest(BaseModel):
    seance: Seance
    available_rooms: List[Room]
    margin_ratio: float = 0.10

@app.post("/recommend_room")
async def recommend_room(request: RecommendationRequest):
    if not request.available_rooms:
        return {"message": "Aucune salle disponible.", "recommended_room": None}

    seance = request.seance
    # Calculate expected number of students
    expected_students = seance.salle_capacite_initiale * seance.moy_presence_groupe_30j
    # Add margin
    target_capacity = expected_students * (1 + request.margin_ratio)
    
    scored_rooms = []
    for room in request.available_rooms:
        if not room.is_available:
            continue
            
        # 1. Capacity Score (Higher is better)
        # We want capacity >= target_capacity, but not too large
        if room.capacite < expected_students:
            score = 0.1 # Too small, very low score
        else:
            # Ideal is room.capacite == target_capacity
            diff = abs(room.capacite - target_capacity)
            score = 1.0 / (1.0 + (diff / target_capacity))
            
        # 2. Type/Mode influence (optional)
        if seance.mode.lower() == "présentiel" and room.capacite >= seance.salle_capacite_initiale:
            score += 0.2 # Favor full capacity for presential
            
        scored_rooms.append((score, room))
    
    if not scored_rooms:
        return {"message": "Aucune salle adéquate trouvée.", "recommended_room": None}
        
    # Sort by score descending
    scored_rooms.sort(key=lambda x: x[0], reverse=True)
    best_room = scored_rooms[0][1]
    
    return {
        "message": f"L'IA recommande la salle {best_room.id} (Capacité: {best_room.capacite}) pour un effectif attendu de ~{int(expected_students)} étudiants.",
        "recommended_room": best_room
    }

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8003)
