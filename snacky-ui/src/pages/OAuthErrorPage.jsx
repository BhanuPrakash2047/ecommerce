import React, { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Header, Footer } from '@/components/layout';
import { Card, Button } from '@/components/common';
import { AlertCircle } from 'lucide-react';
import { showToast } from '@/utils/toast';

const OAuthErrorPage = () => {
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const errorMessage = params.get('message') || 'OAuth2 authentication failed';
    
    showToast(errorMessage, 'error');
    
    // Auto redirect after 3 seconds
    const timer = setTimeout(() => {
      navigate('/login');
    }, 3000);

    return () => clearTimeout(timer);
  }, [location.search, navigate]);

  return (
    <div className="min-h-screen bg-gradient-to-br from-orange-50 via-white to-emerald-50 flex flex-col">
      <Header />
      <div className="flex-1 flex items-center justify-center px-4 py-12">
        <Card className="p-10 max-w-md w-full text-center">
          <AlertCircle className="w-16 h-16 text-red-500 mx-auto mb-4" />
          <h2 className="text-2xl font-bold text-slate-900 mb-2">Authentication Failed</h2>
          <p className="text-slate-600 text-sm mb-6">
            There was an issue signing in with Google. Please try again.
          </p>
          <Button
            variant="primary"
            className="w-full"
            onClick={() => navigate('/login')}
          >
            Back to Login
          </Button>
        </Card>
      </div>
      <Footer />
    </div>
  );
};

export default OAuthErrorPage;
