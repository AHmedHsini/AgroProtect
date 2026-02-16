"""
ML Face Recognition Service - MVC Entry Point
FastAPI microservice for face embedding extraction and comparison.

Requirements:
- Python 3.9+
- face_recognition library (dlib-based)
- FastAPI + Uvicorn
"""

import logging
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.controllers.face_controller import face_router, health_router
from app.config import ALLOWED_ORIGINS, HOST, PORT

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Create FastAPI application
app = FastAPI(
    title="ML Face Recognition Service",
    description="Face embedding extraction and comparison API using MVC architecture",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc"
)

# CORS configuration
app.add_middleware(
    CORSMiddleware,
    allow_origins=ALLOWED_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers (Controllers)
app.include_router(health_router)
app.include_router(face_router)

# Root endpoint
@app.get("/")
async def root():
    """Root endpoint with API information"""
    return {
        "message": "ML Face Recognition Service",
        "version": "1.0.0",
        "architecture": "MVC",
        "docs": "/docs",
        "health": "/health"
    }


if __name__ == "__main__":
    import uvicorn
    logger.info(f"Starting ML Face Recognition Service on {HOST}:{PORT}")
    uvicorn.run("main_mvc:app", host=HOST, port=PORT, reload=True)