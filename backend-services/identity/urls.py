from django.urls import path

from . import views

urlpatterns = [
    path("sign-in/", views.sign_in, name="sign_in"),
    path("sign-up/", views.sign_up, name="sign_up"),
    path("mfa/", views.multifactor_auth, name="multifactor_auth"),
    path("reset/", views.password_reset, name="password_reset"),
    path("consent/", views.oauth_consent, name="oauth_consent"),
]
