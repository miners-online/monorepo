from django.urls import path
from status.views import index

urlpatterns = [
    path("", index),
]
