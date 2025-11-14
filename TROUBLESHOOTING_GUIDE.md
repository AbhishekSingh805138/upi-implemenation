# 🔧 Troubleshooting Guide - Utility Services

## ✅ Quick Checklist

### 1. Browser Refresh
- [ ] Hard refresh: **Ctrl+Shift+R** (Chrome/Edge) or **Ctrl+F5** (Firefox)
- [ ] Clear cache if needed
- [ ] Try incognito/private window

### 2. Check Login Status
```javascript
// Open browser console (F12) and run:
const user = localStorage.getItem('currentUser');
console.log('Current User:', JSON.parse(user));
```

**Expected Output:**
```json
{
  "id": 1,
  "username": "ajit",
  "email": "ajit@example.com",
  "fullName": "Ajit Kumar",
  "phoneNumber": "9876543210"
}
```

**If null**: Logout and login again

### 3. Check Services Running
```bash
# Check if all services are running
netstat -ano | findstr "8761 8080 8081 8082 8083 8084 4200"
```

**Expected Ports:**
- 8761: Eureka Server ✅
- 8080: API Gateway ✅
- 8081: User Service ✅
- 8082: Account Service ✅
- 8083: Transaction Service ✅
- 8084: Utility Service ✅
- 4200: Frontend ✅

---

## 🐛 Common Issues & Solutions

### Issue 1: "User not logged in"
**Symptoms**: Error message on page load

**Solution**:
1. Logout from application
2. Clear localStorage: `localStorage.clear()`
3. Login again
4. Refresh page

---

### Issue 2: Buttons Not Clickable
**Symptoms**: Clicking buttons does nothing

**Solution**:
1. Check browser console for errors (F12)
2. Hard refresh: Ctrl+Shift+R
3. Check if Angular compiled successfully
4. Look for red errors in terminal

---

### Issue 3: "Failed to load..."
**Symptoms**: API call errors

**Solution**:
1. Check backend service is running
2. Check API Gateway is running
3. Test API directly:
   ```bash
   curl http://localhost:8080/api/utilities/categories
   ```
4. Check backend logs for errors

---

### Issue 4: Payment History Empty
**Symptoms**: No payments showing

**Solution**:
1. Make some payments first (mobile/DTH recharge)
2. Check if userId is correct
3. Test API:
   ```bash
   curl http://localhost:8080/api/utilities/payments/1
   ```

---

## 🧪 Testing Each Feature

### Test 1: Electricity Bill
```
1. Go to: http://localhost:4200/utilities
2. Click "Electricity Bill"
3. Should navigate to: /utilities/bills/electricity
4. Check console for errors
5. Enter provider code: BESCOM
6. Enter consumer number: 123456789
7. Click "Fetch Bill Details"
8. Should show bill details
9. Click "Pay Now"
```

**Expected API Calls:**
```
GET  http://localhost:8080/api/utilities/bills/electricity/fetch?providerCode=BESCOM&consumerNumber=123456789
POST http://localhost:8080/api/utilities/bills/electricity
```

---

### Test 2: Credit Card Bill
```
1. Go to: http://localhost:4200/utilities
2. Click "Credit Card Bill"
3. Should navigate to: /utilities/bills/credit-card
4. Check console for errors
5. Select issuer: HDFC
6. Enter last 4 digits: 1234
7. Enter amount: 5000
8. Click "Pay Now"
```

**Expected API Calls:**
```
GET  http://localhost:8080/api/utilities/bills/credit-card/issuers
POST http://localhost:8080/api/utilities/bills/credit-card
```

---

### Test 3: Insurance Premium
```
1. Go to: http://localhost:4200/utilities
2. Click "Insurance Premium"
3. Should navigate to: /utilities/bills/insurance
4. Check console for errors
5. Enter provider: LIC
6. Enter policy number: 123456789
7. Enter amount: 10000
8. Click "Pay Now"
```

**Expected API Calls:**
```
POST http://localhost:8080/api/utilities/bills/insurance
```

---

### Test 4: Payment History
```
1. Go to: http://localhost:4200/utilities/history
2. Should load without errors
3. Should show payments (if any exist)
4. Try filters:
   - Select category
   - Select dates
   - Click Apply
```

**Expected API Calls:**
```
GET http://localhost:8080/api/utilities/payments/1
GET http://localhost:8080/api/utilities/payments/1/daterange?startDate=...&endDate=...
```

---

## 🔍 Debug Steps

### Step 1: Open Browser Console
```
Press F12
Go to Console tab
Look for red errors
```

### Step 2: Check Network Tab
```
Press F12
Go to Network tab
Click on failed requests (red)
Check Response tab for error message
```

### Step 3: Check Backend Logs
```
Look at utility-service console
Look for error messages
Check if API endpoints are being called
```

---

## 📝 Manual API Testing

### Test Electricity Bill API
```bash
# Fetch bill
curl -X GET "http://localhost:8080/api/utilities/bills/electricity/fetch?providerCode=BESCOM&consumerNumber=123456789"

# Pay bill
curl -X POST "http://localhost:8080/api/utilities/bills/electricity" \
  -H "Content-Type: application/json" \
  -d '{
    "upiId": "user@upi",
    "providerCode": "BESCOM",
    "consumerNumber": "123456789",
    "amount": 1000
  }'
```

### Test Credit Card API
```bash
# Get issuers
curl -X GET "http://localhost:8080/api/utilities/bills/credit-card/issuers"

# Pay bill
curl -X POST "http://localhost:8080/api/utilities/bills/credit-card" \
  -H "Content-Type: application/json" \
  -d '{
    "upiId": "user@upi",
    "issuerCode": "HDFC",
    "cardLast4Digits": "1234",
    "amount": 5000
  }'
```

### Test Insurance API
```bash
curl -X POST "http://localhost:8080/api/utilities/bills/insurance" \
  -H "Content-Type: application/json" \
  -d '{
    "upiId": "user@upi",
    "providerCode": "LIC",
    "policyNumber": "123456789",
    "amount": 10000
  }'
```

### Test Payment History API
```bash
# Get all payments
curl -X GET "http://localhost:8080/api/utilities/payments/1"

# Get by date range
curl -X GET "http://localhost:8080/api/utilities/payments/1/daterange?startDate=2025-11-01T00:00:00&endDate=2025-11-14T23:59:59"
```

---

## 🚨 Emergency Reset

If nothing works:

### 1. Stop All Services
```bash
# Stop all Java processes
taskkill /F /IM java.exe

# Stop Angular
# Press Ctrl+C in Angular terminal
```

### 2. Clear Everything
```bash
# Clear browser
- Clear cache
- Clear localStorage
- Close all tabs

# Clear backend
cd utility-service
mvn clean
```

### 3. Restart Everything
```bash
# Start in order:
1. Eureka Server
2. API Gateway
3. User Service
4. Account Service
5. Transaction Service
6. Utility Service
7. Frontend
```

### 4. Fresh Login
```
1. Go to http://localhost:4200
2. Register new user (if needed)
3. Login
4. Create account
5. Try features
```

---

## 📞 Still Not Working?

### Collect This Information:

1. **Browser Console Errors**:
   - Screenshot of console
   - Copy error messages

2. **Network Tab**:
   - Which API calls are failing?
   - What's the response?

3. **Backend Logs**:
   - Any errors in utility-service?
   - Any errors in API gateway?

4. **User Status**:
   - Is user logged in?
   - What's in localStorage?

5. **Service Status**:
   - Are all services running?
   - Check ports

---

## ✅ Success Indicators

### Everything Working When:
- ✅ No console errors
- ✅ API calls return 200 OK
- ✅ User data in localStorage
- ✅ All services running
- ✅ Can navigate to pages
- ✅ Forms load properly
- ✅ Buttons are clickable
- ✅ Payments process successfully

---

**Try these steps and let me know what errors you see!** 🔍
