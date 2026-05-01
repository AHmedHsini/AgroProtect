"""
Application Configuration
"""
import os
import logging
from fastapi import HTTPException, Header
from dotenv import load_dotenv

# Load environment variables from .env file
load_dotenv()

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# API Configuration
API_KEY = os.environ.get("ML_API_KEY", "development-key")
PORT = int(os.environ.get("PORT", 8001))
HOST = os.environ.get("HOST", "0.0.0.0")
ENVIRONMENT = os.environ.get("ENVIRONMENT", "development")
LOG_LEVEL = os.environ.get("LOG_LEVEL", "INFO")

# Face Recognition Configuration
FACE_RECOGNITION_ENABLED = os.environ.get("FACE_RECOGNITION_ENABLED", "true").lower() == "true"
FACE_DETECTION_TOLERANCE = float(os.environ.get("FACE_DETECTION_TOLERANCE", "0.6"))
FACE_COMPARISON_THRESHOLD = float(os.environ.get("FACE_COMPARISON_THRESHOLD", "0.6"))

# CORS Configuration
ALLOWED_ORIGINS = ["*"]  # Configure as needed for production


def verify_api_key(x_api_key: str = Header(None)):
    """
    Verify API key authentication.
    
    Args:
        x_api_key: API key from X-API-Key header
        
    Raises:
        HTTPException: If API key is invalid
        
    Returns:
        str: Valid API key
    """
    if x_api_key != API_KEY:
        raise HTTPException(status_code=401, detail="Invalid API key")
    return x_api_key