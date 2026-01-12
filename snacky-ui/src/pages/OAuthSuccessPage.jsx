import React, { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { Header, Footer } from '@/components/layout';
import { Card, Spinner, Button } from '@/components/common';
import { getProfile } from '@/store/thunks/authThunks';
import { setAuthFromToken } from '@/store/slices/authSlice';
import { showToast } from '@/utils/toast';

const OAuthSuccessPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const dispatch = useDispatch();

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const token = params.get('token');
    const email = params.get('email');
    const errorMessage = params.get('message') || params.get('error');

    if (errorMessage) {
      showToast(errorMessage, 'error');
      navigate('/login');
      return;
    }

    if (!token) {
      showToast('Missing token from Google sign in.', 'error');
      navigate('/login');
      return;
    }

    const completeOAuthLogin = async () => {
      // Persist token so future API calls are authorized
      dispatch(setAuthFromToken({ token }));

      try {
        const profile = await dispatch(getProfile()).unwrap();
        dispatch(setAuthFromToken({ token, user: profile }));
        showToast('Signed in with Google', 'success');
        navigate('/');
      } catch (err) {
        console.error('OAuth profile fetch failed', err);
        showToast('Signed in, but failed to load profile. Please retry.', 'warning');
        navigate('/');
      }
    };

    completeOAuthLogin();
  }, [dispatch, location.search, navigate]);

  return (
    <div className="min-h-screen bg-gradient-to-br from-orange-50 via-white to-emerald-50 flex flex-col">
      <Header />
      <div className="flex-1 flex items-center justify-center px-4 py-12">
        <Card className="p-10 max-w-md w-full text-center">
          <Spinner size="lg" className="mx-auto" />
          <h2 className="mt-4 text-xl font-bold text-slate-900">Completing sign in…</h2>
          <p className="mt-2 text-slate-600 text-sm">
            We are finalizing your Google sign in and preparing your account.
          </p>
          <Button
            variant="link"
            className="mt-4 text-orange-600"
            onClick={() => navigate('/')}
          >
            Go to Home
          </Button>
        </Card>
      </div>
      <Footer />
    </div>
  );
};

export default OAuthSuccessPage;
