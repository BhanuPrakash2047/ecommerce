import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { ProductCard } from './ProductCard';
import { Skeleton } from '../common';
import { fetchAllProducts } from '@/store/thunks/productThunks';

/**
 * ProductGrid Component
 * Displays products in responsive grid with loading states
 */
export const ProductGrid = ({
  products = null,
  columns = { xs: 1, sm: 2, md: 3, lg: 4 },
  gap = 'gap-6',
  loading = false,
  showLoadMore = false,
  onLoadMore = null,
  autoFetch = false,
  categoryGradients = {},
}) => {
  const dispatch = useDispatch();
  const { items: reduxProducts, loading: reduxLoading } = useSelector(state => state.products);

  const productsToDisplay = products || reduxProducts;
  const isLoading = loading || reduxLoading;

  useEffect(() => {
    if (autoFetch && !products) {
      dispatch(fetchAllProducts());
    }
  }, [autoFetch, dispatch, products]);

  const gridColsClass = `
    grid-cols-${columns.xs}
    sm:grid-cols-${columns.sm || columns.xs}
    md:grid-cols-${columns.md || columns.sm || columns.xs}
    lg:grid-cols-${columns.lg || columns.md || columns.sm || columns.xs}
  `;

  const getCategoryGradient = (product) => {
    const category = product.category?.toLowerCase() || 'default';
    return categoryGradients[category] || 'from-brand-500 to-accent-600';
  };

  // Skeleton loading state
  if (isLoading && productsToDisplay.length === 0) {
    return (
      <div className={`grid ${gap} md:grid-cols-3 lg:grid-cols-4`}>
        {Array(8)
          .fill(null)
          .map((_, i) => (
            <ProductCard key={i} loading={true} />
          ))}
      </div>
    );
  }

  // Empty state
  if (!isLoading && productsToDisplay.length === 0) {
    return (
      <div className="text-center py-16">
        <div className="text-5xl mb-4">📭</div>
        <h3 className="text-2xl font-bold text-gray-900 mb-2">No Products Found</h3>
        <p className="text-gray-600">Try adjusting your filters or search term</p>
      </div>
    );
  }

  return (
    <>
      <div className={`grid ${gap} grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4`}>
        {productsToDisplay.map(product => (
          <ProductCard
            key={product.id}
            product={product}
            categoryGradient={getCategoryGradient(product)}
            loading={false}
          />
        ))}
      </div>

      {/* Load More Button */}
      {showLoadMore && onLoadMore && (
        <div className="flex justify-center mt-12">
          <button
            onClick={onLoadMore}
            className="px-8 py-3 bg-gradient-brand text-white font-bold rounded-lg hover:shadow-brand hover:shadow-lg transition-all duration-300 transform hover:scale-105 active:scale-95"
          >
            Load More Products
          </button>
        </div>
      )}
    </>
  );
};

export default ProductGrid;
