/* eslint-disable no-unused-vars */
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { User, Mail, Phone, MapPin, LogOut, Lock, Upload, Eye, EyeOff } from 'lucide-react';
import { Header, Footer } from '@/components/layout';
import { Button, Input, Card, Modal } from '@/components/common';
import { updateProfile, logoutUser, changePassword } from '@/store/thunks/authThunks';
import { showToast } from '@/utils/toast';
import { useFormValidation } from '@/hooks';

const UserProfilePage = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { user, loading } = useSelector(state => state.auth);

  const { 
    formData: profileForm, 
    errors: profileErrors, 
    touched: profileTouched, 
    handleChange: handleProfileChange, 
    handleBlur: handleProfileBlur,
    validateForm: validateProfileForm,
    getFieldError: getProfileFieldError,
    setFormValues: setProfileFormValues
  } = useFormValidation({
    fullName: '',
    email: '',
    phone: '',
  });

  const profileValidationRules = {
    fullName: 'fullName',
    email: 'email',
    phone: 'phone',
  };

  const [editingProfile, setEditingProfile] = useState(false);
  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const [showOldPassword, setShowOldPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [changingPassword, setChangingPassword] = useState(false);
  const [updatingProfile, setUpdatingProfile] = useState(false);

  const {
    formData: passwordForm,
    errors: passwordErrors,
    touched: passwordTouched,
    handleChange: handlePasswordChange,
    handleBlur: handlePasswordBlur,
    validateForm: validatePasswordForm,
    getFieldError: getPasswordFieldError,
    setFormValues: setPasswordFormValues
  } = useFormValidation({
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
  });

  const passwordValidationRules = {
    oldPassword: 'required',
    newPassword: 'password',
    confirmPassword: (value) => {
      if (!value.trim()) return 'Please confirm your password';
      if (value !== passwordForm.newPassword) return 'Passwords do not match';
      return null;
    },
  };

  // Initialize form with user data
  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }

    setProfileFormValues({
      fullName: user.fullName || '',
      email: user.email || '',
      phone: user.phone || '',
    });
  }, [user, navigate]);

  const handleUpdateProfile = async (e) => {
    e.preventDefault();

    if (!validateProfileForm(profileForm, profileValidationRules)) {
      return;
    }

    setUpdatingProfile(true);
    try {
      await dispatch(updateProfile(profileForm)).unwrap();
      showToast('Profile updated successfully!', 'success');
      setEditingProfile(false);
    } catch (err) {
      showToast(err || 'Failed to update profile', 'error');
    } finally {
      setUpdatingProfile(false);
    }
  };

  const handleChangePassword = async (e) => {
    e.preventDefault();

    if (!validatePasswordForm(passwordForm, passwordValidationRules)) {
      return;
    }

    setChangingPassword(true);
    try {
      await dispatch(changePassword({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword,
        confirmPassword: passwordForm.confirmPassword,
      })).unwrap();

      showToast('Password changed successfully!', 'success');

      setPasswordFormValues({
        oldPassword: '',
        newPassword: '',
        confirmPassword: '',
      });
      setShowPasswordModal(false);
    } catch (err) {
      showToast(err || 'Failed to change password', 'error');
    } finally {
      setChangingPassword(false);
    }
  };

  const handleLogout = () => {
    if (window.confirm('Are you sure you want to logout?')) {
      dispatch(logoutUser());
      showToast('Logged out successfully', 'success');
      navigate('/');
    }
  };

  if (!user) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-slate-50 to-white">
      <Header />

      <div className="max-w-4xl mx-auto px-4 py-12">
        {/* Header */}
        <div className="mb-12">
          <h1 className="text-4xl font-bold text-slate-900 mb-2">My Profile</h1>
          <p className="text-slate-600">Manage your account information</p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Sidebar */}
          <div>
            {/* Profile Card */}
            <Card className="p-6 text-center mb-6">
              <div className="w-24 h-24 bg-gradient-to-br from-orange-500 to-emerald-500 rounded-full mx-auto flex items-center justify-center mb-4">
                <User className="w-12 h-12 text-white" />
              </div>
              <h2 className="text-2xl font-bold text-slate-900 mb-1">{user.fullName}</h2>
              <p className="text-slate-600 text-sm">{user.email}</p>
              <div className="mt-6 pt-6 border-t border-slate-200">
                <p className="text-xs font-semibold text-slate-600 uppercase mb-2">Member Since</p>
                <p className="text-sm text-slate-900">
                  {new Date(user.createdAt).toLocaleDateString('en-IN', {
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric',
                  })}
                </p>
              </div>
            </Card>

            {/* Quick Links */}
            <div className="space-y-3">
              <Button
                onClick={() => navigate('/orders')}
                variant="outline"
                className="w-full justify-start"
              >
                📦 My Orders
              </Button>
              <Button
                onClick={() => navigate('/addresses')}
                variant="outline"
                className="w-full justify-start"
              >
                <MapPin className="w-4 h-4 mr-2" />
                Addresses
              </Button>
              <Button
                onClick={() => setShowPasswordModal(true)}
                variant="outline"
                className="w-full justify-start"
              >
                <Lock className="w-4 h-4 mr-2" />
                Change Password
              </Button>
            </div>
          </div>

          {/* Main Content */}
          <div className="lg:col-span-2 space-y-8">
            {/* Profile Information */}
            <Card className="p-8">
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-2xl font-bold text-slate-900">Profile Information</h2>
                {!editingProfile && (
                  <Button
                    onClick={() => setEditingProfile(true)}
                    variant="outline"
                    size="sm"
                  >
                    Edit Profile
                  </Button>
                )}
              </div>

              {editingProfile ? (
                <form onSubmit={handleUpdateProfile} className="space-y-6">
                  {/* Full Name */}
                  <div>
                    <label className="block text-sm font-semibold text-slate-900 mb-2">
                      Full Name
                    </label>
                    <Input
                      type="text"
                      name="fullName"
                      value={profileForm.fullName}
                      onChange={(e) => handleProfileChange(e, profileValidationRules)}
                      onBlur={(e) => handleProfileBlur('fullName', profileValidationRules)}
                      error={getProfileFieldError('fullName')}
                      placeholder="Your full name"
                    />
                  </div>

                  {/* Email (Read-only) */}
                  <div>
                    <label className="block text-sm font-semibold text-slate-900 mb-2">
                      Email Address
                    </label>
                    <Input
                      type="email"
                      value={profileForm.email}
                      disabled
                      className="bg-slate-100 cursor-not-allowed"
                    />
                    <p className="text-xs text-slate-600 mt-2">
                      Email cannot be changed for security reasons
                    </p>
                  </div>

                  {/* Phone */}
                  <div>
                    <label className="block text-sm font-semibold text-slate-900 mb-2">
                      Phone Number
                    </label>
                    <Input
                      type="tel"
                      name="phone"
                      value={profileForm.phone}
                      onChange={(e) => handleProfileChange(e, profileValidationRules)}
                      onBlur={(e) => handleProfileBlur('phone', profileValidationRules)}
                      error={getProfileFieldError('phone')}
                      placeholder="Your phone number"
                    />
                  </div>

                  {/* Buttons */}
                  <div className="flex gap-3">
                    <Button
                      type="submit"
                      variant="primary"
                      disabled={updatingProfile}
                    >
                      {updatingProfile ? 'Saving...' : 'Save Changes'}
                    </Button>
                    <Button
                      type="button"
                      onClick={() => setEditingProfile(false)}
                      variant="outline"
                    >
                      Cancel
                    </Button>
                  </div>
                </form>
              ) : (
                <div className="space-y-6">
                  {/* Full Name */}
                  <div>
                    <label className="block text-xs font-semibold text-slate-600 uppercase mb-2">
                      Full Name
                    </label>
                    <div className="flex items-center gap-2">
                      <User className="w-5 h-5 text-slate-400" />
                      <p className="text-lg text-slate-900">{profileForm.fullName}</p>
                    </div>
                  </div>

                  {/* Email */}
                  <div>
                    <label className="block text-xs font-semibold text-slate-600 uppercase mb-2">
                      Email Address
                    </label>
                    <div className="flex items-center gap-2">
                      <Mail className="w-5 h-5 text-slate-400" />
                      <p className="text-lg text-slate-900">{profileForm.email}</p>
                    </div>
                  </div>

                  {/* Phone */}
                  <div>
                    <label className="block text-xs font-semibold text-slate-600 uppercase mb-2">
                      Phone Number
                    </label>
                    <div className="flex items-center gap-2">
                      <Phone className="w-5 h-5 text-slate-400" />
                      <p className="text-lg text-slate-900">{profileForm.phone}</p>
                    </div>
                  </div>
                </div>
              )}
            </Card>

            {/* Account Settings */}
            <Card className="p-8">
              <h2 className="text-2xl font-bold text-slate-900 mb-6">Account Settings</h2>

              <div className="space-y-4">
                <button
                  onClick={() => setShowPasswordModal(true)}
                  className="w-full p-4 border border-slate-200 rounded-lg hover:border-slate-300 hover:bg-slate-50 transition text-left flex items-center justify-between group"
                >
                  <div>
                    <p className="font-semibold text-slate-900">Change Password</p>
                    <p className="text-sm text-slate-600">Update your account password</p>
                  </div>
                  <span className="text-slate-400 group-hover:text-slate-600">→</span>
                </button>

                <button
                  onClick={handleLogout}
                  className="w-full p-4 border border-red-200 rounded-lg hover:border-red-300 hover:bg-red-50 transition text-left flex items-center justify-between group"
                >
                  <div>
                    <p className="font-semibold text-red-600">Logout</p>
                    <p className="text-sm text-red-600">Sign out of your account</p>
                  </div>
                  <LogOut className="w-5 h-5 text-red-600" />
                </button>
              </div>
            </Card>


          </div>
        </div>
      </div>

      {/* Change Password Modal */}
      <Modal
        isOpen={showPasswordModal}
        onClose={() => {
          setShowPasswordModal(false);
          setPasswordFormValues({
            oldPassword: '',
            newPassword: '',
            confirmPassword: '',
          });
        }}
        title="Change Password"
      >
        <form onSubmit={handleChangePassword} className="space-y-6">
          {/* Old Password */}
          <div>
            <label className="block text-sm font-semibold text-slate-900 mb-2">
              Current Password
            </label>
            <div className="relative">
              <Lock className="absolute left-4 top-3.5 w-5 h-5 text-slate-400" />
              <Input
                type={showOldPassword ? 'text' : 'password'}
                name="oldPassword"
                placeholder="••••••••"
                value={passwordForm.oldPassword}
                onChange={(e) => handlePasswordChange(e, passwordValidationRules)}
                onBlur={(e) => handlePasswordBlur('oldPassword', passwordValidationRules)}
                error={getPasswordFieldError('oldPassword')}
              />
              <button
                type="button"
                onClick={() => setShowOldPassword(!showOldPassword)}
                className="absolute right-4 top-3.5 text-slate-400 hover:text-slate-600 transition"
              >
                {showOldPassword ? (
                  <EyeOff className="w-5 h-5" />
                ) : (
                  <Eye className="w-5 h-5" />
                )}
              </button>
            </div>
          </div>

          {/* New Password */}
          <div>
            <label className="block text-sm font-semibold text-slate-900 mb-2">
              New Password
            </label>
            <div className="relative">
              <Lock className="absolute left-4 top-3.5 w-5 h-5 text-slate-400" />
              <Input
                type={showNewPassword ? 'text' : 'password'}
                name="newPassword"
                placeholder="••••••••"
                value={passwordForm.newPassword}
                onChange={(e) => handlePasswordChange(e, passwordValidationRules)}
                onBlur={(e) => handlePasswordBlur('newPassword', passwordValidationRules)}
                error={getPasswordFieldError('newPassword')}
              />
              <button
                type="button"
                onClick={() => setShowNewPassword(!showNewPassword)}
                className="absolute right-4 top-3.5 text-slate-400 hover:text-slate-600 transition"
              >
                {showNewPassword ? (
                  <EyeOff className="w-5 h-5" />
                ) : (
                  <Eye className="w-5 h-5" />
                )}
              </button>
            </div>
          </div>

          {/* Confirm Password */}
          <div>
            <label className="block text-sm font-semibold text-slate-900 mb-2">
              Confirm New Password
            </label>
            <div className="relative">
              <Lock className="absolute left-4 top-3.5 w-5 h-5 text-slate-400" />
              <Input
                type={showConfirmPassword ? 'text' : 'password'}
                name="confirmPassword"
                placeholder="••••••••"
                value={passwordForm.confirmPassword}
                onChange={(e) => handlePasswordChange(e, passwordValidationRules)}
                onBlur={(e) => handlePasswordBlur('confirmPassword', passwordValidationRules)}
                error={getPasswordFieldError('confirmPassword')}
              />
              <button
                type="button"
                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                className="absolute right-4 top-3.5  text-slate-400 hover:text-slate-600 transition"
              >
                {showConfirmPassword ? (
                  <EyeOff className="w-5 h-5" />
                ) : (
                  <Eye className="w-5 h-5" />
                )}
              </button>
            </div>
          </div>

          {/* Buttons */}
          <div className="flex gap-3">
            <Button
              type="submit"
              variant="primary"
              className="flex-1 bg-orange-600"
              disabled={changingPassword}
            >
              {changingPassword ? 'Changing...' : 'Change Password'}
            </Button>
            <Button
              type="button"
              onClick={() => setShowPasswordModal(false)}
              variant="outline"
              className="flex-1"
            >
              Cancel
            </Button>
          </div>
        </form>
      </Modal>

      <Footer />
    </div>
  );
};

export default UserProfilePage;
