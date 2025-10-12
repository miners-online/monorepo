from datetime import datetime
from django.http import HttpResponse, JsonResponse


def index(request):
    now = datetime.now()
    html = {
        "status": "ok",
        "timestamp": now.isoformat(),
        "version": "1.0.0",
        "services": [
            {"name": "api", "status": "ok", "timestamp": now.isoformat()},
            {"name": "db", "status": "ok", "timestamp": now.isoformat()},
            {"name": "minecraft-proxy", "status": "ok", "timestamp": now.isoformat()},
        ],
    }
    return JsonResponse(html)
