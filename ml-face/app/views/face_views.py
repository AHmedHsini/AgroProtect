"""
Face Recognition API Request/Response Models (Views)
"""
from pydantic import BaseModel, Field
from typing import Optional


class FaceExtractionRequest(BaseModel):
    """Request model for face embedding extraction"""
    image: str = Field(..., description="Base64 encoded face image")
    detect_liveness: bool = Field(default=True, description="Perform liveness detection")


class FaceExtractionResponse(BaseModel):
    """Response model for face embedding extraction"""
    success: bool
    embedding: Optional[str] = None
    liveness_score: float = Field(default=0.0, alias="livenessScore")
    quality_score: float = Field(default=0.0, alias="qualityScore")
    error: Optional[str] = None

    class Config:
        populate_by_name = True


class FaceComparisonRequest(BaseModel):
    """Request model for face comparison"""
    embedding1: str = Field(..., description="First face embedding (JSON array)")
    embedding2: str = Field(..., description="Second face embedding (JSON array)")


class FaceComparisonResponse(BaseModel):
    """Response model for face comparison"""
    success: bool
    similarity: float = 0.0
    error: Optional[str] = None


class HealthResponse(BaseModel):
    """Health check response model"""
    status: str
    face_recognition_available: bool