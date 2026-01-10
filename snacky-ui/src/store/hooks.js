import { useDispatch, useSelector } from 'react-redux';

// Custom hooks for common Redux operations
export const useAuth = () => {
  const dispatch = useDispatch();
  const auth = useSelector(state => state.auth);
  return { dispatch, ...auth };
};

export const useCart = () => {
  const dispatch = useDispatch();
  const cart = useSelector(state => state.cart);
  return { dispatch, ...cart };
};

export const useProducts = () => {
  const dispatch = useDispatch();
  const products = useSelector(state => state.products);
  return { dispatch, ...products };
};

export const useOrders = () => {
  const dispatch = useDispatch();
  const orders = useSelector(state => state.orders);
  return { dispatch, ...orders };
};

export const usePayment = () => {
  const dispatch = useDispatch();
  const payment = useSelector(state => state.payment);
  return { dispatch, ...payment };
};

export const useDelivery = () => {
  const dispatch = useDispatch();
  const delivery = useSelector(state => state.delivery);
  return { dispatch, ...delivery };
};

export const useCoupons = () => {
  const dispatch = useDispatch();
  const coupons = useSelector(state => state.coupons);
  return { dispatch, ...coupons };
};

export const useNotifications = () => {
  const dispatch = useDispatch();
  const notifications = useSelector(state => state.notifications);
  return { dispatch, ...notifications };
};

export const useAddresses = () => {
  const dispatch = useDispatch();
  const addresses = useSelector(state => state.addresses);
  return { dispatch, ...addresses };
};

// Direct dispatch hook
export const useAppDispatch = () => useDispatch();

// Direct selector hooks
export const useAuthState = () => useSelector(state => state.auth);
export const useCartState = () => useSelector(state => state.cart);
export const useProductsState = () => useSelector(state => state.products);
export const useOrdersState = () => useSelector(state => state.orders);
export const usePaymentState = () => useSelector(state => state.payment);
export const useDeliveryState = () => useSelector(state => state.delivery);
export const useCouponsState = () => useSelector(state => state.coupons);
export const useNotificationsState = () => useSelector(state => state.notifications);
export const useAddressesState = () => useSelector(state => state.addresses);
