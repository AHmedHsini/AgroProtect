"""
Face Recognition Business Logic (Model)
"""
import base64
import numpy as np
import io
import json
import logging
from typing import Tuple, Optional
from PIL import Image

logger = logging.getLogger(__name__)

try:
    import face_recognition
    FACE_RECOGNITION_AVAILABLE = True
except ImportError:
    logger.warning("face_recognition library not available. Using mock implementation.")
    FACE_RECOGNITION_AVAILABLE = False


class FaceRecognitionModel:
    """Face recognition business logic and utilities"""

    @staticmethod
    def is_face_recognition_available() -> bool:
        """Check if face recognition library is available"""
        return FACE_RECOGNITION_AVAILABLE

    @staticmethod
    def decode_base64_image(base64_string: str) -> np.ndarray:
        """Decode base64 image to numpy array"""
        # Remove data URL prefix if present
        if "," in base64_string:
            base64_string = base64_string.split(",")[1]
        
        image_data = base64.b64decode(base64_string)
        image = Image.open(io.BytesIO(image_data))
        
        # Convert to RGB if necessary
        if image.mode != "RGB":
            image = image.convert("RGB")
        
        return np.array(image)

    @staticmethod
    def encode_embedding(embedding: np.ndarray) -> str:
        """Encode numpy embedding to JSON string"""
        return json.dumps(embedding.tolist())

    @staticmethod
    def decode_embedding(embedding_str: str) -> np.ndarray:
        """Decode JSON string to numpy embedding"""
        return np.array(json.loads(embedding_str))

    @classmethod
    def extract_face_embedding(cls, image: np.ndarray) -> Tuple[Optional[np.ndarray], float, float]:
        """
        Extract face embedding from image.
        
        Args:
            image: Input image as numpy array
            
        Returns:
            Tuple of (embedding, liveness_score, quality_score)
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

    @classmethod
    def compare_embeddings(cls, emb1: np.ndarray, emb2: np.ndarray) -> float:
        """
        Compare two face embeddings.
        
        Args:
            emb1: First face embedding
            emb2: Second face embedding
            
        Returns:
            Similarity score (0-1)
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