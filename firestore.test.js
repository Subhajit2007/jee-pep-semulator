const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read tracker logs", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("trackerLogs").get());
});

test("Authenticated user: cannot read other user's tracker logs", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("trackerLogs").get());
});

test("Authenticated user: can create and read their own tracker log", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const logRef = aliceDb.collection("users").doc(ALICE_UID).collection("trackerLogs").doc("day_1");
  await assertSucceeds(
    logRef.set({
      userId: ALICE_UID,
      dayNumber: 1,
      date: "2026-11-08",
      totalStudyHours: 8.5,
      pyqsSolved: 60,
      physicsHours: 3.0,
      chemistryHours: 2.5,
      mathsHours: 3.0,
      examMarks: 210,
      notes: "Revised Kinematics & Thermodynamics",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("trackerLogs").get());
});

test("Authenticated user: can update their own planner progress", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const progressRef = aliceDb.collection("users").doc(ALICE_UID).collection("plannerProgress").doc("status");
  await assertSucceeds(
    progressRef.set({
      userId: ALICE_UID,
      completedTopicIds: ["phy_day1_1", "chem_day1_1"],
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: can record exam result", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const resultRef = aliceDb.collection("users").doc(ALICE_UID).collection("examResults").doc("exam_1");
  await assertSucceeds(
    resultRef.set({
      userId: ALICE_UID,
      examTitle: "JEE Main Mock 1",
      totalQuestions: 75,
      attemptedCount: 65,
      timeLimitMinutes: 180,
      timeTakenSeconds: 10400,
      nvtRange: "21-25, 46-50, 71-75",
      answersSummary: '{"1":"A","2":"C"}',
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});
