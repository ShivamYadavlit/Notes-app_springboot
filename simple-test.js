// Simple test to verify the application is working
console.log("Testing Notes Application");
console.log("========================");

// Test data
const testUsers = [
  { email: "admin@acme.test", role: "ADMIN" },
  { email: "user@acme.test", role: "MEMBER" },
  { email: "admin@globex.test", role: "ADMIN" },
  { email: "user@globex.test", role: "MEMBER" }
];

console.log("Test accounts available:");
testUsers.forEach(user => {
  console.log(`- ${user.email} (${user.role}) - password: password`);
});

console.log("\nApplication URLs:");
console.log("- Frontend: http://localhost:5173");
console.log("- Backend API: http://localhost:8080");
console.log("- Health check: http://localhost:8080/health");

console.log("\nTo test the application:");
console.log("1. Open http://localhost:5173 in your browser");
console.log("2. Login with any of the test accounts above");
console.log("3. Create, edit, and delete notes");
console.log("4. Test different roles:");
console.log("   - Admin users can upgrade tenant plans");
console.log("   - Regular users have limited access");
console.log("   - Users can only access their own notes");