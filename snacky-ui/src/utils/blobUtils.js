/**
 * Utility functions for handling image and video blobs from backend
 */

/**
 * Convert blob data to Base64 string for img src
 * @param {Blob} blob - Blob data from backend
 * @returns {Promise<string>} - Base64 encoded string
 */
export const blobToBase64 = (blob) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onloadend = () => {
      resolve(reader.result);
    };
    reader.onerror = reject;
    reader.readAsDataURL(blob);
  });
};

/**
 * Convert blob to data URL for immediate use
 * @param {Blob} blob - Blob data from backend
 * @returns {string} - Object URL
 */
export const blobToDataUrl = (blob) => {
  return URL.createObjectURL(blob);
};

/**
 * Display product image from blob
 * @param {Blob} imageBlob - Image blob from backend
 * @returns {Promise<string>} - Data URL for img element
 */
export const getImageUrl = async (imageBlob) => {
  if (!imageBlob) return null;
  
  if (imageBlob instanceof Blob) {
    return blobToBase64(imageBlob);
  }
  
  // If already a string/URL
  return imageBlob;
};

/**
 * Get video source from blob
 * @param {Blob} videoBlob - Video blob from backend
 * @returns {string} - Object URL for video element
 */
export const getVideoUrl = (videoBlob) => {
  if (!videoBlob) return null;
  
  if (videoBlob instanceof Blob) {
    return blobToDataUrl(videoBlob);
  }
  
  return videoBlob;
};

/**
 * Cleanup object URL when component unmounts
 * @param {string} url - Object URL to revoke
 */
export const revokeUrl = (url) => {
  if (url && url.startsWith('blob:')) {
    URL.revokeObjectURL(url);
  }
};

/**
 * Format image array from backend
 * @param {Array} images - Array of image objects with blob data
 * @returns {Promise<Array>} - Array of image objects with data URLs
 */
export const formatProductImages = async (images) => {
  if (!images || images.length === 0) return [];
  
  const formattedImages = await Promise.all(
    images.map(async (image) => ({
      ...image,
      dataUrl: await getImageUrl(image.imageBlob || image.data)
    }))
  );
  
  return formattedImages;
};

/**
 * Format video array from backend
 * @param {Array} videos - Array of video objects with blob data
 * @returns {Array} - Array of video objects with data URLs
 */
export const formatProductVideos = (videos) => {
  if (!videos || videos.length === 0) return [];
  
  const formattedVideos = videos.map((video) => ({
    ...video,
    dataUrl: getVideoUrl(video.videoBlob || video.data)
  }));
  
  return formattedVideos;
};

/**
 * Download file from blob
 * @param {Blob} blob - File blob (e.g., shipping label PDF)
 * @param {string} filename - Desired filename for download
 */
export const downloadFile = (blob, filename = 'download') => {
  const url = blobToDataUrl(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  revokeUrl(url);
};
