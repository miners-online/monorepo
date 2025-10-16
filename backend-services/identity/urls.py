from django.urls import path, include

from . import views

urlpatterns = [
    path("", include("django.contrib.auth.urls")),
    path("sign-up/", views.sign_up, name="sign_up"),
    path("mfa/", views.multifactor_auth, name="multifactor_auth"),
    path("consent/", views.oauth_consent, name="oauth_consent"),
]
