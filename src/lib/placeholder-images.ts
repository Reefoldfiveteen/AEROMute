
import data from './placeholder-images.json';

export type ImagePlaceholder = {
  id: string;
  description: string;
  imageUrl: string;
  imageHint: string;
};

// Defensive check to ensure placeholderImages is always a valid array to prevent "undefined" errors during SSR/Module evaluation
export const PlaceHolderImages: ImagePlaceholder[] = (data as any)?.placeholderImages || [];

export const getPlaceholderImage = (index: number): string => {
  const fallback = 'https://picsum.photos/seed/app/100/100';
  try {
    const images = PlaceHolderImages;
    if (images && images[index]) {
      return images[index].imageUrl || fallback;
    }
  } catch (e) {
    // Silent fail to fallback
  }
  return fallback;
};
