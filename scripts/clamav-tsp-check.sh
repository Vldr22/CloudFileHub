#!/bin/bash
docker exec -it cloudfilehub-clamav sh -c "clamdscan --ping 1"

docker exec -it antivirus-service sh -c "nc -zv cloudfilehub-clamav 3310"

docker events --filter container=cloudfilehub-clamav