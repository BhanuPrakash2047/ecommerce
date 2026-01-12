/**
 * File upload utilities for product images and videos
 */

/**
 * Validate image file
 * @param {File} file - File to validate
 * @param {number} maxSizeMB - Maximum file size in MB
 * @returns {Object} - { isValid, error }
 */
export const validateImageFile = (file, maxSizeMB = 5) => {
  const validTypes = ['image/jpeg', 'image/png', 'image/webp'];
  const maxSizeBytes = maxSizeMB * 1024 * 1024;

  if (!file) {
    return { isValid: false, error: 'No file selected' };
  }

  if (!validTypes.includes(file.type)) {
    return { isValid: false, error: 'Invalid image format. Allowed: JPG, PNG, WebP' };
  }

  if (file.size > maxSizeBytes) {
    return { isValid: false, error: `File size must be less than ${maxSizeMB}MB` };
  }

  return { isValid: true, error: null };
};

/**
 * Validate video file
 * @param {File} file - File to validate
 * @param {number} maxSizeMB - Maximum file size in MB
 * @returns {Object} - { isValid, error }
 */
export const validateVideoFile = (file, maxSizeMB = 100) => {
  const validTypes = ['video/mp4', 'video/webm', 'video/quicktime'];
  const maxSizeBytes = maxSizeMB * 1024 * 1024;

  if (!file) {
    return { isValid: false, error: 'No file selected' };
  }

  if (!validTypes.includes(file.type)) {
    return { isValid: false, error: 'Invalid video format. Allowed: MP4, WebM, MOV' };
  }

  if (file.size > maxSizeBytes) {
    return { isValid: false, error: `File size must be less than ${maxSizeMB}MB` };
  }

  return { isValid: true, error: null };
};

/**
 * Get video duration from file
 * @param {File} file - Video file
 * @returns {Promise<number>} - Duration in seconds
 */
export const getVideoDuration = (file) => {
  return new Promise((resolve, reject) => {
    const video = document.createElement('video');
    const objectUrl = URL.createObjectURL(file);

    video.onloadedmetadata = () => {
      URL.revokeObjectURL(objectUrl);
      resolve(Math.round(video.duration));
    };

    video.onerror = () => {
      URL.revokeObjectURL(objectUrl);
      reject(new Error('Failed to load video'));
    };

    video.src = objectUrl;
  });
};

/**
 * Get image preview as data URL
 * @param {File} file - Image file
 * @returns {Promise<string>} - Data URL
 */
export const getImagePreview = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result);
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
};

/**
 * Get video preview thumbnail
 * @param {File} file - Video file
 * @param {number} atTime - Time in seconds to capture thumbnail
 * @returns {Promise<string>} - Data URL of thumbnail
 */
export const getVideoThumbnail = (file, atTime = 0) => {
  return new Promise((resolve, reject) => {
    const video = document.createElement('video');
    const canvas = document.createElement('canvas');
    const objectUrl = URL.createObjectURL(file);

    video.onloadedmetadata = () => {
      video.currentTime = Math.min(atTime, video.duration);
    };

    video.onseeked = () => {
      canvas.width = video.videoWidth;
      canvas.height = video.videoHeight;
      const ctx = canvas.getContext('2d');
      ctx.drawImage(video, 0, 0);
      URL.revokeObjectURL(objectUrl);
      resolve(canvas.toDataURL('image/jpeg'));
    };

    video.onerror = () => {
      URL.revokeObjectURL(objectUrl);
      reject(new Error('Failed to load video'));
    };

    video.src = objectUrl;
  });
};

/**
 * Format file size for display
 * @param {number} bytes - File size in bytes
 * @returns {string} - Formatted size (e.g., "2.5 MB")
 */
export const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
};

/**
 * Upload progress calculator
 * @param {ProgressEvent} event - Upload progress event
 * @returns {number} - Progress percentage (0-100)
 */
export const calculateUploadProgress = (event) => {
  if (!event.lengthComputable) return 0;
  return Math.round((event.loaded / event.total) * 100);
};
