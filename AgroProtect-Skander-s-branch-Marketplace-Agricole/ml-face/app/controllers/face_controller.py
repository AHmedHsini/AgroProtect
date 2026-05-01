"""
Face Recognition API Controllers (Route Handlers)
"""
import logging
from fastapi import APIRouter, HTTPException, Depends
from app.views.face_views import (
    FaceExtractionRequest, FaceExtractionResponse,
    FaceComparisonRequest, FaceComparisonResponse,
    HealthResponse
)
from app.models.face_model import FaceRecognitionModel
from app.config import verify_api_key

logger = logging.getLogger(__name__)

# Create router for face recognition endpoints
face_router = APIRouter(prefix="/api/v1/face", tags=["Face Recognition"])


@face_router.post("/extract", response_model=FaceExtractionResponse)
async def extract_face_embedding(
    request: FaceExtractionRequest, 
    api_key: str = Depends(verify_api_key)
):
    """
    Extract face embedding from image.
    
    This endpoint takes a base64-encoded image and returns the face embedding
    along with quality and liveness scores.
    """
    try:
        # Decode image
        image = FaceRecognitionModel.decode_base64_image(request.image)
        
        # Extract embedding
        embedding, liveness_score, quality_score = FaceRecognitionModel.extract_face_embedding(image)
        
        if embedding is None:
            return FaceExtractionResponse(
                success=False,
                error="No face detected in image"
            )
        
        return FaceExtractionResponse(
            success=True,
            embedding=FaceRecognitionModel.encode_embedding(embedding),
            liveness_score=liveness_score,
            quality_score=quality_score
        )
        
    except Exception as e:
        logger.error(f"Face extraction error: {e}")
        return FaceExtractionResponse(
            success=False,
            error=str(e)
        )


@face_router.post("/compare", response_model=FaceComparisonResponse)
async def compare_face_embeddings(
    request: FaceComparisonRequest,
    api_key: str = Depends(verify_api_key)
):
    """
    Compare two face embeddings.
    
    This endpoint compares two face embeddings and returns a similarity score.
    """
    try:
        emb1 = FaceRecognitionModel.decode_embedding(request.embedding1)
        emb2 = FaceRecognitionModel.decode_embedding(request.embedding2)
        
        similarity = FaceRecognitionModel.compare_embeddings(emb1, emb2)
        
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


# Health check router
health_router = APIRouter(tags=["Health"])


@health_router.get("/health", response_model=HealthResponse)
async def health_check():
    """
    Health check endpoint.
    
    Returns the service status and face recognition library availability.
    """
    return HealthResponse(
        status="ok",
        face_recognition_available=FaceRecognitionModel.is_face_recognition_available()
    )