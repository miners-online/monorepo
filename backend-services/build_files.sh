python3.12 -m venv venv
source venv/bin/activate
python3.12 -m pip install -r requirements.txt
python3.12 manage.py collectstatic --noinput