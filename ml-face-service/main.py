"""
ML Face Recognition Service
FastAPI microservice for face embedding extraction and comparison.

Requirements:
- Python 3.9+
- face_recognition library (dlib-based)
- FastAPI + Uvicorn
"""

from fastapi import FastAPI, HTTPException, Header, Depends
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
import base64
import numpy as np
import io
import os
import logging

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="ML Face Recognition Service",
    description="Face embedding extraction and comparison API",
    version="1.0.0"
)

# CORS configuration
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# API Key validation
API_KEY = os.environ.get("ML_API_KEY", "development-key")

def verify_api_key(x_api_key: str = Header(None)):
    if x_api_key != API_KEY:
        raise HTTPException(status_code=401, detail="Invalid API key")
    return x_api_key


# ==================== Models ====================

class FaceExtractionRequest(BaseModel):
    image: str = Field(..., description="Base64 encoded face image")
    detect_liveness: bool = Field(default=True, description="Perform liveness detection")

class FaceExtractionResponse(BaseModel):
    success: bool
    embedding: str | None = None
    liveness_score: float = Field(default=0.0, alias="livenessScore")
    quality_score: float = Field(default=0.0, alias="qualityScore")
    error: str | None = None

    class Config:
        populate_by_name = True

class FaceComparisonRequest(BaseModel):
    embedding1: str = Field(..., description="First face embedding (JSON array)")
    embedding2: str = Field(..., description="Second face embedding (JSON array)")

class FaceComparisonResponse(BaseModel):
    success: bool
    similarity: float = 0.0
    error: str | None = None


# ==================== Face Recognition Logic ====================

try:
    import face_recognition
    from PIL import Image
    FACE_RECOGNITION_AVAILABLE = True
except ImportError:
    logger.warning("face_recognition library not available. Using mock implementation.")
    FACE_RECOGNITION_AVAILABLE = False


def decode_base64_image(base64_string: str) -> np.ndarray:
    """Decode base64 image to numpy array."""
    # Remove data URL prefix if present
    if "," in base64_string:
        base64_string = base64_string.split(",")[1]
    
    image_data = base64.b64decode(base64_string)
    image = Image.open(io.BytesIO(image_data))
    
    # Convert to RGB if necessary
    if image.mode != "RGB":
        image = image.convert("RGB")
    
    return np.array(image)


def encode_embedding(embedding: np.ndarray) -> str:
    """Encode numpy embedding to JSON string."""
    import json
    return json.dumps(embedding.tolist())


def decode_embedding(embedding_str: str) -> np.ndarray:
    """Decode JSON string to numpy embedding."""
    import json
    return np.array(json.loads(embedding_str))


def extract_face_embedding_impl(image: np.ndarray) -> tuple[np.ndarray | None, float, float]:
    """
    Extract face embedding from image.
    Returns: (embedding, liveness_score, quality_score)
    """
    if not FACE_RECOGNITION_AVAILABLE:
        # Mock implementation for testing
        mock_embedding = np.random.rand(128).astype(np.float32)
        return mock_embedding, 0.95, 0.92
    
    # Find faces in image
    face_locations = face_recognition.face_locations(image, model="hog")
    
    if len(face_locations) == 0:
        return None, 0.0, 0.0
    
    if len(face_locations) > 1:
        # Multiple faces detected - take the largest
        face_locations = [max(face_locations, key=lambda x: (x[2]-x[0]) * (x[1]-x[3]))]
    
    # Extract face encoding
    encodings = face_recognition.face_encodings(image, face_locations)
    
    if len(encodings) == 0:
        return None, 0.0, 0.0
    
    embedding = encodings[0]
    
    # Calculate quality score based on face size and position
    face = face_locations[0]
    face_height = face[2] - face[0]
    face_width = face[1] - face[3]
    
    # Simple quality metric based on face size ratio
    quality_score = min(1.0, (face_height * face_width) / (image.shape[0] * image.shape[1] * 0.1))
    
    # Liveness detection (simplified - in production use specialized model)
    # For now, assume live if quality is good
    liveness_score = 0.95 if quality_score > 0.5 else 0.3
    
    return embedding, liveness_score, quality_score


def compare_embeddings_impl(emb1: np.ndarray, emb2: np.ndarray) -> float:
    """
    Compare two face embeddings.
    Returns: similarity score (0-1)
    """
    if not FACE_RECOGNITION_AVAILABLE:
        # Mock: return random similarity
        return 0.92
    
    # Use face_recognition's built-in comparison
    distance = face_recognition.face_distance([emb1], emb2)[0]
    
    # Convert distance to similarity (0-1 scale)
    # face_recognition uses Euclidean distance, typically < 0.6 for same person
    similarity = max(0.0, 1.0 - distance)
    
    return float(similarity)


# ==================== Endpoints ====================

@app.get("/health")
async def health_check():
    """Health check endpoint."""
    return {
        "status": "ok",
        "face_recognition_available": FACE_RECOGNITION_AVAILABLE
    }


@app.post("/api/v1/face/extract", response_model=FaceExtractionResponse)
async def extract_face(request: FaceExtractionRequest, api_key: str = Depends(verify_api_key)):
    """Extract face embedding from image."""
    try:
        # Decode image
        image = decode_base64_image(request.image)
        
        # Extract embedding
        embedding, liveness_score, quality_score = extract_face_embedding_impl(image)
        
        if embedding is None:
            return FaceExtractionResponse(
                success=False,
                error="No face detected in image"
            )
        
        return FaceExtractionResponse(
            success=True,
            embedding=encode_embedding(embedding),
            liveness_score=liveness_score,
            quality_score=quality_score
        )
        
    except Exception as e:
        logger.error(f"Face extraction error: {e}")
        return FaceExtractionResponse(
            success=False,
            error=str(e)
        )


@app.post("/api/v1/face/compare", response_model=FaceComparisonResponse)
async def compare_faces(request: FaceComparisonRequest, api_key: str = Depends(verify_api_key)):
    """Compare two face embeddings."""
    try:
        emb1 = decode_embedding(request.embedding1)
        emb2 = decode_embedding(request.embedding2)
        
        similarity = compare_embeddings_impl(emb1, emb2)
        
        return FaceComparisonResponse(
            success=True,
            similarity=similarity
        )
        
    except Exception as e:
        logger.error(f"Face comparison error: {e}")
        return FaceComparisonResponse(
            success=False,
            error=str(e)
        )


if __name__ == "__main__":
    import uvicorn
    port = int(os.environ.get("PORT", 8001))
    uvicorn.run(app, host="0.0.0.0", port=port)
