from django_hosts import patterns, host

host_patterns = patterns(
    "",
    host(r"api", "api.urls", name="api"),
    host(r"auth", "identity.urls", name="identity"),
    host(r"admin", "api.admin_urls", name="admin"),
)
