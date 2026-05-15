#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# test-rate-limit.sh
# Validación manual del rate limit en los endpoints públicos de auth.
#
# Uso:
#   ./test-rate-limit.sh                        # apunta a localhost:8080
#   ./test-rate-limit.sh https://api.example.com
#
# Espera:
#   - Peticiones 1-5  → 200 (o 4xx de negocio, NO 429)
#   - Petición 6+     → 429 Too Many Requests
# ---------------------------------------------------------------------------

BASE_URL="${1:-http://localhost:8080}"
ENDPOINTS=(
  "/api/v1/auth/request-otp"
  "/api/v1/auth/verify-otp"
  "/api/v1/auth/refresh"
)
TOTAL_REQUESTS=8   # supera el límite de 5 para provocar 429
PASS=0
FAIL=0

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

assert_status() {
  local label="$1"
  local expected="$2"
  local actual="$3"

  if [ "$actual" -eq "$expected" ]; then
    echo -e "  ${GREEN}PASS${NC} $label → HTTP $actual"
    ((PASS++))
  else
    echo -e "  ${RED}FAIL${NC} $label → esperado HTTP $expected, obtenido HTTP $actual"
    ((FAIL++))
  fi
}

run_endpoint_test() {
  local path="$1"
  local url="${BASE_URL}${path}"

  echo ""
  echo -e "${YELLOW}── $path ──${NC}"

  for i in $(seq 1 $TOTAL_REQUESTS); do
    status=$(curl -s -o /dev/null -w "%{http_code}" \
      -X POST "$url" \
      -H "Content-Type: application/json" \
      -d '{}')

    if [ "$i" -le 5 ]; then
      # Las primeras 5 peticiones no deben ser bloqueadas por rate limit (429)
      if [ "$status" -eq 429 ]; then
        assert_status "Petición $i (no debería ser 429)" 999 429
      else
        echo -e "  ${GREEN}PASS${NC} Petición $i → HTTP $status (no bloqueada)"
        ((PASS++))
      fi
    else
      # A partir de la 6ª debe devolver 429
      assert_status "Petición $i (rate limit)" 429 "$status"
    fi
  done
}

# ---------------------------------------------------------------------------
echo "================================================="
echo "  Rate Limit Test — ${BASE_URL}"
echo "  Límite configurado: 5 req/min por IP"
echo "================================================="

for endpoint in "${ENDPOINTS[@]}"; do
  run_endpoint_test "$endpoint"
done

# ---------------------------------------------------------------------------
echo ""
echo "================================================="
echo -e "  Resultado: ${GREEN}${PASS} PASS${NC} / ${RED}${FAIL} FAIL${NC}"
echo "================================================="

# ---------------------------------------------------------------------------
echo ""
echo -e "${YELLOW}── Espera 60s y reintenta para verificar que el bucket se rellena ──${NC}"
read -r -p "  ¿Esperar y revalidar? [s/N] " confirm
if [[ "$confirm" =~ ^[sS]$ ]]; then
  echo "  Esperando 61 segundos..."
  sleep 61

  echo ""
  echo -e "${YELLOW}── Verificación post-reset (primera petición debe pasar) ──${NC}"
  for endpoint in "${ENDPOINTS[@]}"; do
    status=$(curl -s -o /dev/null -w "%{http_code}" \
      -X POST "${BASE_URL}${endpoint}" \
      -H "Content-Type: application/json" \
      -d '{}')

    if [ "$status" -eq 429 ]; then
      assert_status "Post-reset $endpoint" 999 429
    else
      echo -e "  ${GREEN}PASS${NC} Post-reset $endpoint → HTTP $status (bucket rellenado)"
      ((PASS++))
    fi
  done

  echo ""
  echo "================================================="
  echo -e "  Resultado final: ${GREEN}${PASS} PASS${NC} / ${RED}${FAIL} FAIL${NC}"
  echo "================================================="
fi

[ "$FAIL" -eq 0 ] && exit 0 || exit 1
