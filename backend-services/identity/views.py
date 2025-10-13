from django.shortcuts import render


def sign_in(request):
    return render(request, "identity/sign_in.html")


def sign_up(request):
    return render(request, "identity/sign_up.html")


def multifactor_auth(request):
    return render(request, "identity/multifactor_auth.html")


def password_reset(request):
    return render(request, "identity/password_reset.html")


def oauth_consent(request):
    return render(request, "identity/oauth_consent.html")
