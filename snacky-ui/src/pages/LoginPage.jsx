/* eslint-disable no-unused-vars */
import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { Mail, Lock, Eye, EyeOff, ArrowRight, Zap, Award, Heart } from 'lucide-react';
import { Header, Footer } from '@/components/layout';
import { Button, Input } from '@/components/common';
import { loginUser } from '@/store/thunks/authThunks';
import { showToast } from '@/utils/toast';
import { useFormValidation } from '@/hooks';

const LoginPage = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { user, loading, error } = useSelector(state => state.auth);

  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(false);
  const [visibleFeatures, setVisibleFeatures] = useState(0);
  
  const {
    formData,

    handleChange,
    handleBlur,
    validateForm,
    getFieldError,
    setFormValues,
  } = useFormValidation({
    email: '',
    password: '',
  });

  const validationRules = {
    email: 'email',
    password: 'password',
  };

  const features = [
    { icon: <Zap className="w-8 h-8" />, title: 'Lower Prices', desc: 'Best prices without compromising on quality'},
    { icon: <Award className="w-8 h-8" />, title: 'Quality Guaranteed', desc: 'Fresh, premium snacks every time' },
    {  icon: <Heart className="w-8 h-8" />, title: 'Hand-Curated', desc: 'Picked by us, crafted with care, love, and passion' },
  ];

  // Auto-rotate features on mobile
  useEffect(() => {
    const timer = setInterval(() => {
      setVisibleFeatures((prev) => (prev + 1) % features.length);
    }, 4000);
    return () => clearInterval(timer);
  }, [features.length]);

  const apiBase = import.meta.env.VITE_API_URL;
  const authServerBase = apiBase.replace(/\/api$/, '');

  // Redirect if already logged in
  useEffect(() => {
    if (user) {
      navigate('/');
    }
  }, [user, navigate]);

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm(formData, validationRules)) {
      showToast('Please fix the errors below', 'error');
      return;
    }

    try {
      await dispatch(loginUser(formData)).unwrap();
      showToast('Login successful! Welcome back.', 'success');
      navigate('/');
    } catch (err) {
      showToast(err || 'Login failed. Please try again.', 'error');
    }
  };

  const handleDemoLogin = () => {
    setFormValues({
      email: 'demo@example.com',
      password: 'demo123456',
    });
  };

  const handleGoogleLogin = () => {
    const redirectUri = `${window.location.origin}/oauth2/success`;
    const url = `${authServerBase}/oauth2/authorize/google?redirect_uri=${encodeURIComponent(redirectUri)}`;
    window.location.href = url;
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-orange-50 via-white to-emerald-50">
      <Header />

      <div className="max-w-6xl mx-auto px-4 py-8 lg:py-12">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 lg:gap-12 items-center min-h-[600px]">
          {/* Left Side - Branding (Visible on all screens) */}
          <div className="order-2 lg:order-1">
            {/* Mobile Features Carousel */}
            <div className="lg:hidden mb-8">
              <style>{`
                @keyframes slideInUp {
                  from {
                    opacity: 0;
                    transform: translateY(20px);
                  }
                  to {
                    opacity: 1;
                    transform: translateY(0);
                  }
                }
                
                @keyframes fadeInScale {
                  from {
                    opacity: 0;
                    transform: scale(0.95);
                  }
                  to {
                    opacity: 1;
                    transform: scale(1);
                  }
                }

                @keyframes gradientShift {
                  0%, 100% {
                    background-position: 0% 50%;
                  }
                  50% {
                    background-position: 100% 50%;
                  }
                }

                @keyframes bounce-custom {
                  0%, 100% {
                    transform: translateY(0);
                  }
                  50% {
                    transform: translateY(-8px);
                  }
                }
                
                .animate-slideInUp {
                  animation: slideInUp 0.8s ease-out forwards;
                }
                
                .animate-fadeInScale {
                  animation: fadeInScale 0.6s ease-out;
                }

                .animate-gradient-shift {
                  background-size: 200% 200%;
                  animation: gradientShift 3s ease infinite;
                }

                .animate-bounce-custom {
                  animation: bounce-custom 2s ease-in-out infinite;
                }
              `}</style>

              <div className="bg-white rounded-2xl border-2 border-orange-200 p-6 shadow-lg">
                <div className="text-center">
                  <div className="mb-4 flex justify-center animate-fadeInScale">
                    <div className="text-orange-600">
                      {features[visibleFeatures].icon}
                    </div>
                  </div>
                  <h3 className="text-2xl font-bold text-orange-600 mb-2">
                    {features[visibleFeatures].title}
                  </h3>
                  <p className="text-slate-600">
                    {features[visibleFeatures].desc}
                  </p>
                </div>
                
                {/* Carousel indicators */}
                <div className="flex justify-center gap-2 mt-4">
                  {features.map((_, idx) => (
                    <button
                      key={idx}
                      onClick={() => setVisibleFeatures(idx)}
                      className={`transition-all duration-300 rounded-full ${
                        idx === visibleFeatures 
                          ? 'bg-orange-600 w-3 h-3' 
                          : 'bg-orange-200 w-2 h-2 hover:bg-orange-300'
                      }`}
                    />
                  ))}
                </div>
              </div>
            </div>

            {/* Desktop Features Section */}
            <div className="hidden lg:block space-y-8 animate-slideInUp">
              <div className="animate-slideInUp" style={{ animationDelay: '0.1s' }}>
                <h1 className="text-5xl font-bold bg-gradient-to-r from-orange-600 via-orange-500 to-emerald-600 bg-clip-text text-transparent mb-4 animate-gradient-shift">
                  Welcome Back to Snacky
                </h1>
                <p className="text-xl text-slate-600">
                  Your favorite snacks delivered to your doorstep
                </p>
              </div>

              <div className="space-y-6">
                {features.map((item, idx) => (
                  <div 
                    key={idx} 
                    className="flex gap-4 group animate-slideInUp hover:translate-x-1 transition-transform duration-300"
                    style={{ animationDelay: `${0.2 + idx * 0.1}s` }}
                  >
                    <div className="text-orange-600 group-hover:scale-110 transition-transform duration-300 flex-shrink-0">
                      {item.icon}
                    </div>
                    <div>
                      <h3 className="font-bold text-slate-900 group-hover:text-orange-600 transition-colors">{item.title}</h3>
                      <p className="text-slate-600">{item.desc}</p>
                    </div>
                  </div>
                ))}
              </div>

              <div className="bg-gradient-to-r from-orange-50 to-emerald-50 rounded-xl p-6 border border-orange-200 transform hover:scale-105 transition-transform duration-300 animate-slideInUp" style={{ animationDelay: '0.5s' }}>
                <p className="text-sm text-slate-600 mb-4">
                  Don't have an account?
                </p>
                <Link
                  to="/signup"
                  className="inline-flex items-center gap-2 text-orange-600 font-bold hover:text-orange-700 transition group"
                >
                  Create an account
                  <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition" />
                </Link>
              </div>
            </div>
          </div>

          {/* Right Side - Login Form */}
          <div className="order-1 lg:order-2 animate-slideInUp" style={{ animationDelay: '0.3s' }}>
            <div className="bg-white rounded-2xl border-2 border-orange-200 shadow-2xl overflow-hidden hover:shadow-3xl transition-shadow duration-300">
              {/* Header */}
              <div className="bg-gradient-to-r from-orange-600 to-orange-500 px-6 lg:px-8 py-6 lg:py-8 relative overflow-hidden">
                <div className="absolute top-0 right-0 w-40 h-40 bg-white opacity-5 rounded-full -mr-20 -mt-20"></div>
                <div className="relative z-10">
                  <h2 className="text-3xl lg:text-4xl font-bold text-white">Login</h2>
                  <p className="text-orange-50 mt-1">Sign in to your Snacky account</p>
                </div>
              </div>

              {/* Form */}
              <form onSubmit={handleSubmit} className="p-6 lg:p-8 space-y-6">
                {/* Email */}
                <div className="animate-slideInUp" style={{ animationDelay: '0.4s' }}>
                  <label className="block text-sm font-semibold text-slate-900 mb-2">
                    Email Address
                  </label>
                  <div className="relative group">
                    <Mail className="absolute left-4 top-3.5 w-5 h-5 text-orange-400 group-focus-within:text-orange-600 transition-colors" />
                    <Input
                      type="email"
                      name="email"
                      placeholder="you@example.com"
                      value={formData.email}
                      onChange={(e) => handleChange(e, validationRules)}
                      onBlur={(e) => handleBlur('email', validationRules)}
                      error={getFieldError('email')}
                      className="pl-12 border-2 border-slate-200 focus:border-orange-500 focus:ring-2 focus:ring-orange-200 transition-all"
                    />
                  </div>
                </div>

                {/* Password */}
                <div className="animate-slideInUp" style={{ animationDelay: '0.5s' }}>
                  <label className="block text-sm font-semibold text-slate-900 mb-2">
                    Password
                  </label>
                  <div className="relative group">
                    <Lock className="absolute left-4 top-3.5 w-5 h-5 text-orange-400 group-focus-within:text-orange-600 transition-colors" />
                    <Input
                      type={showPassword ? 'text' : 'password'}
                      name="password"
                      placeholder="••••••••"
                      value={formData.password}
                      onChange={(e) => handleChange(e, validationRules)}
                      onBlur={(e) => handleBlur('password', validationRules)}
                      error={getFieldError('password')}
                      className="pl-12 pr-12 border-2 border-slate-200 focus:border-orange-500 focus:ring-2 focus:ring-orange-200 transition-all"
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="absolute right-4 top-3.5 text-slate-400 hover:text-orange-600 transition-colors duration-200 hover:scale-110"
                    >
                      {showPassword ? (
                        <EyeOff className="w-5 h-5" />
                      ) : (
                        <Eye className="w-5 h-5" />
                      )}
                    </button>
                  </div>
                </div>

                {/* Remember & Forgot */}
                <div className="flex items-center justify-between animate-slideInUp" style={{ animationDelay: '0.6s' }}>
                  <label className="flex items-center gap-2 cursor-pointer group">
                    <input
                      type="checkbox"
                      checked={rememberMe}
                      onChange={(e) => setRememberMe(e.target.checked)}
                      className="w-4 h-4 accent-orange-500 rounded cursor-pointer"
                    />
                    <span className="text-sm text-slate-700 group-hover:text-orange-600 transition-colors">Remember me</span>
                  </label>
                  <Link
                    to="/forgot-password"
                    className="text-sm text-orange-600 hover:text-orange-700 font-medium transition-colors"
                  >
                    Forgot password?
                  </Link>
                </div>

                {/* Error Message */}
                {error && (
                  <div className="bg-red-50 border-2 border-red-200 rounded-lg p-4 text-red-700 text-sm animate-slideInUp">
                    {error}
                  </div>
                )}

                {/* Submit Button */}
                <Button
                  type="submit"
                  variant="primary"
                  className="w-full py-3 font-bold text-lg bg-gradient-to-r from-orange-600 to-orange-500 hover:from-orange-700 hover:to-orange-600 text-white rounded-lg transform hover:scale-105 active:scale-95 transition-all duration-200 shadow-lg hover:shadow-xl animate-slideInUp"
                  style={{ animationDelay: '0.7s' }}
                  disabled={loading}
                >
                  {loading ? 'Logging in...' : 'Login'}
                </Button>

                {/* Divider */}
                <div className="relative">
                  <div className="absolute inset-0 flex items-center">
                    <div className="w-full border-t-2 border-slate-200"></div>
                  </div>
                  <div className="relative flex justify-center text-sm">
                    <span className="px-2 bg-white text-slate-500 font-medium">Or continue with</span>
                  </div>
                </div>

                {/* Google Login */}
                <Button
                  type="button"
                  onClick={handleGoogleLogin}
                  variant="outline"
                  className="w-full py-3 flex items-center justify-center gap-3 border-2 border-slate-200 hover:border-orange-300 hover:bg-orange-50 text-slate-700 rounded-lg font-semibold transition-all duration-200 hover:scale-105 active:scale-95 animate-slideInUp"
                  style={{ animationDelay: '0.8s' }}
                >
                  <svg
                    className="w-5 h-5"
                    viewBox="0 0 24 24"
                    fill="none"
                    xmlns="http://www.w3.org/2000/svg"
                  >
                    <path
                      d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
                      fill="#4285F4"
                    />
                    <path
                      d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
                      fill="#34A853"
                    />
                    <path
                      d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
                      fill="#FBBC05"
                    />
                    <path
                      d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
                      fill="#EA4335"
                    />
                  </svg>
                  Sign in with Google
                </Button>
              </form>

              {/* Footer */}
              <div className="border-t-2 border-slate-200 bg-gradient-to-r from-orange-50 to-emerald-50 px-6 lg:px-8 py-5 text-center">
                <p className="text-slate-600 text-sm">
                  Don't have an account?{' '}
                  <Link
                    to="/signup"
                    className="text-orange-600 font-bold hover:text-orange-700 transition-colors"
                  >
                    Sign up now
                  </Link>
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <Footer />
    </div>
  );
};

export default LoginPage;
