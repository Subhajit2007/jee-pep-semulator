package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.PlannerProgress
import com.example.data.model.PlannerTopic
import com.example.data.model.Subject
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class PlannerRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observePlannerProgress(): Flow<Set<String>> = flow {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("plannerProgress").document("status")
        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        val progress = snapshot.toObject(PlannerProgress::class.java)
                        progress?.completedTopicIds?.toSet() ?: emptySet()
                    } else {
                        emptySet()
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, docRef.path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveCompletedTopics(topicIds: Set<String>) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("plannerProgress").document("status")
        val payload = mapOf(
            "userId" to uid,
            "completedTopicIds" to topicIds.toList(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    // Comprehensive 70-Day Study Plan for JEE Main & Advanced
    fun getCurated70DayTopics(): List<PlannerTopic> {
        val list = mutableListOf<PlannerTopic>()

        val daysSyllabus = listOf(
            // Phase 1: Mechanics, Physical Chem, Calculus & Algebra (Days 1 - 25)
            Triple(1, Subject.PHYSICS, "Units, Dimensions & Errors | Significant figures & Vernier calipers"),
            Triple(1, Subject.CHEMISTRY, "Some Basic Concepts of Chemistry | Mole concept & stoichiometry"),
            Triple(1, Subject.MATHS, "Sets, Relations & Functions | Domain, range, inverse & composites"),

            Triple(2, Subject.PHYSICS, "Kinematics 1D | Graphs, calculus methods & relative motion"),
            Triple(2, Subject.CHEMISTRY, "Atomic Structure | Bohr's model & photoelectric effect"),
            Triple(2, Subject.MATHS, "Trigonometric Ratios & Identities | Transformation formulas"),

            Triple(3, Subject.PHYSICS, "Kinematics 2D & Projectile | Trajectory equation & oblique planes"),
            Triple(3, Subject.CHEMISTRY, "Quantum Numbers & Electronic Configuration | Heisenberg principle"),
            Triple(3, Subject.MATHS, "Trigonometric Equations & Heights | General solution principles"),

            Triple(4, Subject.PHYSICS, "Newton's Laws of Motion | Free body diagrams & constrained systems"),
            Triple(4, Subject.CHEMISTRY, "Periodic Table & Periodic Properties | Ionization energy trends"),
            Triple(4, Subject.MATHS, "Quadratic Equations | Roots location & common roots theorem"),

            Triple(5, Subject.PHYSICS, "Friction & Pseudo Forces | Block on block, inclined plane friction"),
            Triple(5, Subject.CHEMISTRY, "Chemical Bonding: VSEPR & Hybridization | Molecular geometries"),
            Triple(5, Subject.MATHS, "Complex Numbers: Basics & Geometry | Modulus-amplitude form"),

            Triple(6, Subject.PHYSICS, "Circular Motion | Centripetal acceleration & banking of roads"),
            Triple(6, Subject.CHEMISTRY, "Molecular Orbital Theory & Dipole Moment | Bond order & paramagnetism"),
            Triple(6, Subject.MATHS, "Complex Numbers: De Moivre's & Roots of Unity | Geometry of circles"),

            Triple(7, Subject.PHYSICS, "Work, Energy & Power | Work-energy theorem & conservative forces"),
            Triple(7, Subject.CHEMISTRY, "Chemical Thermodynamics: 1st Law | Internal energy, enthalpy, work"),
            Triple(7, Subject.MATHS, "Sequences & Series: AP, GP, HP | Sum formulas & telescoping series"),

            Triple(8, Subject.PHYSICS, "Potential Energy Curves & Power | Stable equilibrium & springs"),
            Triple(8, Subject.CHEMISTRY, "Thermodynamics: Entropy & 2nd Law | Gibbs free energy spontaneity"),
            Triple(8, Subject.MATHS, "AGP & Special Series | Sigma summation & method of differences"),

            Triple(9, Subject.PHYSICS, "Center of Mass & Linear Momentum | Discrete & continuous mass, conservation"),
            Triple(9, Subject.CHEMISTRY, "Thermochemistry | Hess's law & bond dissociation energies"),
            Triple(9, Subject.MATHS, "Permutations & Combinations: Basics | Fundamental principle & nCr"),

            Triple(10, Subject.PHYSICS, "Collisions: Elastic & Inelastic | Coefficient of restitution & impulses"),
            Triple(10, Subject.CHEMISTRY, "Chemical Equilibrium | Le Chatelier's principle & equilibrium constants"),
            Triple(10, Subject.MATHS, "Permutations & Combinations: Advanced | Distribution, derangements"),

            Triple(11, Subject.PHYSICS, "Rotational Dynamics: Moment of Inertia | Parallel & perpendicular axis theorems"),
            Triple(11, Subject.CHEMISTRY, "Ionic Equilibrium: pH & Buffer | Weak acids, bases, Henderson equation"),
            Triple(11, Subject.MATHS, "Binomial Theorem | General term, greatest term & series coefficients"),

            Triple(12, Subject.PHYSICS, "Torque, Angular Momentum & Equilibrium | Rolling without slipping"),
            Triple(12, Subject.CHEMISTRY, "Ionic Equilibrium: Solubility Product | Ksp, common ion effect & salt hydrolysis"),
            Triple(12, Subject.MATHS, "Straight Lines: Forms & Angle | Pair of straight lines & concurrency"),

            Triple(13, Subject.PHYSICS, "Gravitation: Kepler's Laws & Field | Gravitational potential & escape speed"),
            Triple(13, Subject.CHEMISTRY, "Redox Reactions & Balancing | Oxidation states & ion-electron method"),
            Triple(13, Subject.MATHS, "Circles: Equations & Tangents | Intercepts, parametric form & chord of contact"),

            Triple(14, Subject.PHYSICS, "Mechanical Properties of Solids | Stress-strain curve & Young's modulus"),
            Triple(14, Subject.CHEMISTRY, "Solid State (Crystalline Lattices) | Unit cells, packing efficiency, voids"),
            Triple(14, Subject.MATHS, "Circles: System of Circles & Common Tangents | Radical axis & family of circles"),

            Triple(15, Subject.PHYSICS, "Fluid Statics & Pascal's Law | Buoyancy, Archimedes & manometer"),
            Triple(15, Subject.CHEMISTRY, "Solutions: Concentration & Raoult's Law | Ideal & non-ideal liquid mixtures"),
            Triple(15, Subject.MATHS, "Parabola: Standard Forms & Tangents | Focal properties & normals"),

            Triple(16, Subject.PHYSICS, "Fluid Dynamics & Bernoulli's Principle | Continuity equation, Torricelli's law"),
            Triple(16, Subject.CHEMISTRY, "Colligative Properties | Osmotic pressure & Van't Hoff factor"),
            Triple(16, Subject.MATHS, "Ellipse: Standard Equations & Eccentricity | Auxiliary circles & director circle"),

            Triple(17, Subject.PHYSICS, "Surface Tension & Viscosity | Capillary action & Stokes' law"),
            Triple(17, Subject.CHEMISTRY, "Electrochemistry: Nernst Equation | EMF, electrode potentials & galvanic cells"),
            Triple(17, Subject.MATHS, "Hyperbola: Standard & Asymptotes | Rectangular hyperbola & tangents"),

            Triple(18, Subject.PHYSICS, "Thermal Expansion & Calorimetry | Specific heat & latent heat transfers"),
            Triple(18, Subject.CHEMISTRY, "Conductance & Kohlrausch's Law | Electrolytic conductance & cells"),
            Triple(18, Subject.MATHS, "Matrices & Determinants: Properties | Cramers rule & inverse matrices"),

            Triple(19, Subject.PHYSICS, "Kinetic Theory of Gases | Maxwell distribution & degrees of freedom"),
            Triple(19, Subject.CHEMISTRY, "Chemical Kinetics: Rate Laws | Integrated rate equations & half-life"),
            Triple(19, Subject.MATHS, "Limits: Standard Limits & L'Hopital | Indeterminate forms & expansion series"),

            Triple(20, Subject.PHYSICS, "Thermodynamics: Heat Engines & Carnot Cycle | Cyclic processes & indicator diagrams"),
            Triple(20, Subject.CHEMISTRY, "Arrhenius Equation & Collision Theory | Activation energy & catalysts"),
            Triple(20, Subject.MATHS, "Continuity & Differentiability | Intermediate value theorem & differentiability checks"),

            // Days 21 - 45: Electrodynamics, Organic Chem & Integral Calculus
            Triple(21, Subject.PHYSICS, "Simple Harmonic Motion: Equations | Kinematics & dynamics of spring mass"),
            Triple(21, Subject.CHEMISTRY, "Surface Chemistry & Adsorption | Freundlich isotherm & colloids"),
            Triple(21, Subject.MATHS, "Differentiation: Chain Rule & Parametric | Higher order derivatives"),

            Triple(22, Subject.PHYSICS, "Simple & Physical Pendulums | Superposition of SHMs & Lissajous"),
            Triple(22, Subject.CHEMISTRY, "GOC: Electronic Effects | Inductive, resonance & hyperconjugation"),
            Triple(22, Subject.MATHS, "Application of Derivatives: Tangents & Normals | Angle of intersection"),

            Triple(23, Subject.PHYSICS, "Wave Motion & Sound Waves | Wave equation, speed in mediums & intensity"),
            Triple(23, Subject.CHEMISTRY, "Aromaticity & Acidity/Basicity | Huckel's rule & stability of carbocations"),
            Triple(23, Subject.MATHS, "Monotonicity: Increasing & Decreasing | Critical points & Rolle's theorem"),

            Triple(24, Subject.PHYSICS, "Doppler Effect & Standing Waves | Organ pipes, beats & resonance tube"),
            Triple(24, Subject.CHEMISTRY, "Isomerism: Structural & Stereoisomerism | Enantiomers, diastereomers & optical activity"),
            Triple(24, Subject.MATHS, "Maxima & Minima | First & second derivative tests, optimization problems"),

            Triple(25, Subject.PHYSICS, "Electrostatics: Coulomb's Law & Electric Field | Continuous charge distributions"),
            Triple(25, Subject.CHEMISTRY, "Hydrocarbons: Alkanes & Free Radical Mechanism | Halogenation & Wurtz reaction"),
            Triple(25, Subject.MATHS, "Indefinite Integration: Substitution | Standard algebraic & trigonometric forms"),

            Triple(26, Subject.PHYSICS, "Gauss's Law & Applications | Spherical, cylindrical & planar symmetry"),
            Triple(26, Subject.CHEMISTRY, "Alkenes: Electrophilic Addition | Markovnikov rule, hydration & ozonolysis"),
            Triple(26, Subject.MATHS, "Integration by Parts & Partial Fractions | Reduction formulas"),

            Triple(27, Subject.PHYSICS, "Electric Potential & Potential Energy | Conservative fields & dipoles"),
            Triple(27, Subject.CHEMISTRY, "Alkynes: Preparation & Reactions | Acidic nature & polymerization"),
            Triple(27, Subject.MATHS, "Definite Integration: Fundamental Properties | King's property & symmetry integrals"),

            Triple(28, Subject.PHYSICS, "Capacitors: Parallel Plate & Dielectrics | Energy stored & force between plates"),
            Triple(28, Subject.CHEMISTRY, "Haloalkanes: SN1 & SN2 Mechanisms | Stereochemical inversion & rate factors"),
            Triple(28, Subject.MATHS, "Definite Integration as Limit of a Sum | Periodic functions integration"),

            Triple(29, Subject.PHYSICS, "Combination of Capacitors & RC Circuits | Charging, discharging & time constant"),
            Triple(29, Subject.CHEMISTRY, "Elimination Reactions: E1 & E2 | Saytzeff vs Hofmann products"),
            Triple(29, Subject.MATHS, "Area Under Curves | Standard curves, symmetric bounds & bounded regions"),

            Triple(30, Subject.PHYSICS, "Current Electricity: Ohm's Law & Drift Speed | Resistivity temperature dependence"),
            Triple(30, Subject.CHEMISTRY, "Haloarenes & Electrophilic Substitution | Directing nature of halogens"),
            Triple(30, Subject.MATHS, "Differential Equations: Variable Separable | Homogeneous differential equations"),

            Triple(31, Subject.PHYSICS, "Kirchhoff's Laws & Resistor Networks | Nodal analysis & bridge circuits"),
            Triple(31, Subject.CHEMISTRY, "Alcohols: Preparation & Chemical Properties | Lucas test, dehydration mechanism"),
            Triple(31, Subject.MATHS, "Linear Differential Equations & Integrating Factor | Bernoulli's form"),

            Triple(32, Subject.PHYSICS, "Potentiometer, Meter Bridge & Galvanometer | Shunt calculations & sensitivity"),
            Triple(32, Subject.CHEMISTRY, "Phenols: Acidity & Kolbe/Reimer-Tiemann | Bromination & coupling reactions"),
            Triple(32, Subject.MATHS, "Vector Algebra: Dot & Cross Product | Geometrical significance & coplanarity"),

            Triple(33, Subject.PHYSICS, "Magnetic Force on Charges & Currents | Lorentz force, cyclotron & helical path"),
            Triple(33, Subject.CHEMISTRY, "Ethers: Williamson Synthesis & Cleavage | Cleavage by HI and mechanism"),
            Triple(33, Subject.MATHS, "Vector Triple Product & Scalar Triple Product | Box product identities"),

            Triple(34, Subject.PHYSICS, "Biot-Savart Law & Ampere's Circuital Law | Solenoid, toroid & long wires"),
            Triple(34, Subject.CHEMISTRY, "Aldehydes & Ketones: Nucleophilic Addition | Cyanohydrin & Grignard additions"),
            Triple(34, Subject.MATHS, "3D Geometry: Direction Cosines & Lines | Shortest distance between skew lines"),

            Triple(35, Subject.PHYSICS, "Magnetic Dipole & Earth's Magnetism | Magnetic materials: dia, para, ferro"),
            Triple(35, Subject.CHEMISTRY, "Aldol Condensation & Cannizzaro Reaction | Cross-aldol & mechanism"),
            Triple(35, Subject.MATHS, "3D Geometry: Planes & Intersections | Distance of point from plane"),

            Triple(36, Subject.PHYSICS, "Electromagnetic Induction: Faraday's & Lenz's Law | Motional EMF"),
            Triple(36, Subject.CHEMISTRY, "Carboxylic Acids: Acidic Strength & Derivatives | Decarboxylation & HVZ reaction"),
            Triple(36, Subject.MATHS, "Probability: Classical & Conditional Probability | Multiplication theorem"),

            Triple(37, Subject.PHYSICS, "Self & Mutual Inductance, LR Circuits | Growth & decay of current"),
            Triple(37, Subject.CHEMISTRY, "Amines: Basicity & Hoffmann Bromamide | Carbylamine test & Hinsberg test"),
            Triple(37, Subject.MATHS, "Bayes' Theorem & Total Probability | Posterior probability calculations"),

            Triple(38, Subject.PHYSICS, "Alternating Current: RMS & Phasor Diagrams | Series LCR resonance"),
            Triple(38, Subject.CHEMISTRY, "Diazonium Salts & Sandmeyer Reaction | Synthetic utility in aromatics"),
            Triple(38, Subject.MATHS, "Probability Distributions: Binomial & Expectation | Mean, variance & random variables"),

            Triple(39, Subject.PHYSICS, "AC Power, Q-factor & Transformers | Power factor & core losses"),
            Triple(39, Subject.CHEMISTRY, "Biomolecules: Carbohydrates & Glucose | Haworth structures & reducing sugars"),
            Triple(39, Subject.MATHS, "Statistics: Mean, Variance & Standard Deviation | Shift of origin & scale"),

            Triple(40, Subject.PHYSICS, "Electromagnetic Waves & Displacement Current | Maxwell equations & spectrum"),
            Triple(40, Subject.CHEMISTRY, "Proteins & Nucleic Acids | Peptide linkage, denaturing, DNA/RNA"),
            Triple(40, Subject.MATHS, "Mathematical Reasoning & Truth Tables | Contrapositive, converse & tautology"),

            // Days 41 - 70: Optics, Modern Physics, Inorganic Chem & Advanced Mock Revisions
            Triple(41, Subject.PHYSICS, "Ray Optics: Reflection & Spherical Mirrors | Mirror formula & magnification"),
            Triple(41, Subject.CHEMISTRY, "Polymers & Everyday Chemistry | Addition, condensation polymers & drugs"),
            Triple(41, Subject.MATHS, "Inverse Trigonometric Functions: Properties | Principal values & summation series"),

            Triple(42, Subject.PHYSICS, "Refraction at Plane Surfaces & Total Internal Reflection | Critical angle & prisms"),
            Triple(42, Subject.CHEMISTRY, "s-Block Elements: Alkali Metals | Hydration enthalpy & flame tests"),
            Triple(42, Subject.MATHS, "Properties of Triangles: Sine & Cosine Rule | Incircle, excircles & medians"),

            Triple(43, Subject.PHYSICS, "Refraction at Spherical Surfaces & Thin Lenses | Lens Maker's formula"),
            Triple(43, Subject.CHEMISTRY, "Alkaline Earth Metals & Anomalous Beryllium | Solubility trends & sulfates"),
            Triple(43, Subject.MATHS, "Determinants: Homogeneous Equations | Non-trivial solutions conditions"),

            Triple(44, Subject.PHYSICS, "Optical Instruments: Microscope & Telescope | Resolving power & magnifying power"),
            Triple(44, Subject.CHEMISTRY, "p-Block Group 13: Boron Family | Diborane structure & back bonding"),
            Triple(44, Subject.MATHS, "Calculus Mixed PYQ Marathon | High-frequency JEE Advanced questions"),

            Triple(45, Subject.PHYSICS, "Wave Optics: Huygens' Principle & Interference | Young's Double Slit Experiment"),
            Triple(45, Subject.CHEMISTRY, "p-Block Group 14: Carbon Family | Allotropes, silicates & silicones"),
            Triple(45, Subject.MATHS, "Algebra High-Weightage Problem Sets | Binomial & sequence patterns"),

            Triple(46, Subject.PHYSICS, "YDSE with Dielectric Slab & Coherent Sources | Fringe shift calculations"),
            Triple(46, Subject.CHEMISTRY, "p-Block Group 15: Nitrogen Family | Ammonia, nitric acid & oxides of nitrogen"),
            Triple(46, Subject.MATHS, "Coordinate Geometry: Tangent-Normal Sprint | Conic section focal chords"),

            Triple(47, Subject.PHYSICS, "Diffraction & Single Slit Pattern | Central maxima width & minima conditions"),
            Triple(47, Subject.CHEMISTRY, "p-Block Group 16: Oxygen Family | Ozone, sulfuric acid & oxoacids"),
            Triple(47, Subject.MATHS, "Vectors & 3D Master Drill | Line-plane intersections & shortest distances"),

            Triple(48, Subject.PHYSICS, "Polarization & Brewster's Law | Malus law & polaroids"),
            Triple(48, Subject.CHEMISTRY, "p-Block Group 17: Halogens & Interhalogens | Oxidizing power & pseudo halogens"),
            Triple(48, Subject.MATHS, "Differential Equations & Curve Family Applications | Orthogonal trajectories"),

            Triple(49, Subject.PHYSICS, "Dual Nature of Radiation & Matter | Photoelectric effect equations & stopping potential"),
            Triple(49, Subject.CHEMISTRY, "p-Block Group 18: Noble Gases | Xenon fluorides and oxofluorides geometries"),
            Triple(49, Subject.MATHS, "Definite Integrals Special Tricks | Fractional part & floor function integrals"),

            Triple(50, Subject.PHYSICS, "De Broglie Wavelength & Davisson-Germer | Matter waves of accelerated particles"),
            Triple(50, Subject.CHEMISTRY, "d & f Block: Transition Elements Trends | Variable oxidation states & magnetic moments"),
            Triple(50, Subject.MATHS, "Complex Numbers Coordinate Geometry | Concyclic points, Apollonius circles"),

            Triple(51, Subject.PHYSICS, "Atomic Structure: Bohr Model & Energy Levels | Spectral series of Hydrogen"),
            Triple(51, Subject.CHEMISTRY, "Potassium Permanganate (KMnO4) & Dichromate | Oxidizing actions & stoichiometry"),
            Triple(51, Subject.MATHS, "Permutations in Geometry | Grid paths, polygons, circular permutations"),

            Triple(52, Subject.PHYSICS, "X-Rays: Characteristic & Continuous | Moseley's law & Bragg's law"),
            Triple(52, Subject.CHEMISTRY, "Coordination Compounds: Werner's & IUPAC | Ligands & coordination numbers"),
            Triple(52, Subject.MATHS, "Probability & Combinatorics Linkages | Dice & cards multi-stage problems"),

            Triple(53, Subject.PHYSICS, "Nuclear Physics: Binding Energy & Mass Defect | Nuclear density & stability"),
            Triple(53, Subject.CHEMISTRY, "Valence Bond Theory & Crystal Field Theory | Octahedral vs tetrahedral splitting"),
            Triple(53, Subject.MATHS, "Function Invertibility & Functional Equations | Cauchy equations"),

            Triple(54, Subject.PHYSICS, "Radioactivity: Alpha, Beta, Gamma Decay | Decay law, half life & mean life"),
            Triple(54, Subject.CHEMISTRY, "Isomerism in Coordination Complexes | Geometrical & optical isomerism"),
            Triple(54, Subject.MATHS, "Leibnitz Rule of Differentiation | Integration under differentiation"),

            Triple(55, Subject.PHYSICS, "Nuclear Fission & Fusion | Chain reactions & stellar nucleosynthesis"),
            Triple(55, Subject.CHEMISTRY, "Metallurgy: Concentration & Ellingham Diagrams | Pyrometallurgy & refining"),
            Triple(55, Subject.MATHS, "Continuity & Non-Differentiable Points Graphing | Absolute value functions"),

            Triple(56, Subject.PHYSICS, "Semiconductor Physics: p-n Junction Diode | Forward & reverse bias characteristics"),
            Triple(56, Subject.CHEMISTRY, "Qualitative Salt Analysis: Cations Group 0 to III | Flame tests & precipitates"),
            Triple(56, Subject.MATHS, "Integration Reduction Formulas | Definite reduction shortcuts"),

            Triple(57, Subject.PHYSICS, "Zener Diode & Voltage Regulation | Breakdown mechanism"),
            Triple(57, Subject.CHEMISTRY, "Salt Analysis: Anions & Confirmatory Tests | Carbonates, halides, sulfates"),
            Triple(57, Subject.MATHS, "Locus Problems in Conic Sections | Auxiliary angle parameters"),

            Triple(58, Subject.PHYSICS, "Bipolar Junction Transistor & Amplifiers | Transistor as a switch & CE mode"),
            Triple(58, Subject.CHEMISTRY, "Environmental Chemistry | Acid rain, ozone depletion & smog types"),
            Triple(58, Subject.MATHS, "Common Tangents & Normals to Conics | Tangent conditions comparisons"),

            Triple(59, Subject.PHYSICS, "Logic Gates & Truth Tables | NAND, NOR universal gates & Boolean algebra"),
            Triple(59, Subject.CHEMISTRY, "Organic Name Reactions Master Sprint | Cannizzaro, Reimer-Tiemann, Aldol"),
            Triple(59, Subject.MATHS, "Matrices Cayley-Hamilton Theorem & Powers | Inversion equations"),

            Triple(60, Subject.PHYSICS, "Communication Systems: Modulation Index | Bandwidth & antenna length"),
            Triple(60, Subject.CHEMISTRY, "Physical Chemistry Formula Revision Sprint | 100 core formulas test"),
            Triple(60, Subject.MATHS, "Calculus Extreme Marathon | 25 multi-concept JEE questions"),

            // Days 61 - 70: Full Length Mock Drills, High-Yield Formulas & Error Analysis
            Triple(61, Subject.PHYSICS, "Mechanics Complete Speed Revision | 30 PYQs with speed timer"),
            Triple(61, Subject.CHEMISTRY, "Inorganic NCERT Line-by-Line Revision | s-block & p-block tables"),
            Triple(61, Subject.MATHS, "Algebra Speed Sprint | Matrices, sequence & quadratics"),

            Triple(62, Subject.PHYSICS, "Electrodynamics Rapid Sprint | Gauss, circuits & induction formula drill"),
            Triple(62, Subject.CHEMISTRY, "Organic Synthesis Roadmaps | Multi-step organic conversions"),
            Triple(62, Subject.MATHS, "Coordinate Geometry Speed Sprint | Tangent & chord properties"),

            Triple(63, Subject.PHYSICS, "Optics & Waves Master Revision | Interference, lenses & sound formulas"),
            Triple(63, Subject.CHEMISTRY, "Equilibrium & Thermodynamics Sprint | Buffer calculations & spontaneity"),
            Triple(63, Subject.MATHS, "Differential Equations & Area Fast Test | 20 mixed PYQs"),

            Triple(64, Subject.PHYSICS, "Modern Physics 100% Accuracy Drill | Photoelectric, Bohr & nuclear"),
            Triple(64, Subject.CHEMISTRY, "Coordination & d-block Rapid Revision | CFT & colors of complexes"),
            Triple(64, Subject.MATHS, "Vectors & 3D High Accuracy Drill | Coplanar and projection problems"),

            Triple(65, Subject.PHYSICS, "Full Mock Drill 1: Physics 25 Questions | 60 mins timed attempt"),
            Triple(65, Subject.CHEMISTRY, "Full Mock Drill 1: Chemistry 25 Questions | 45 mins timed attempt"),
            Triple(65, Subject.MATHS, "Full Mock Drill 1: Maths 25 Questions | 75 mins timed attempt"),

            Triple(66, Subject.PHYSICS, "Error Analysis & Weak Area Rectification | Mechanics & Ray Optics"),
            Triple(66, Subject.CHEMISTRY, "Weak Area Rectification | Organic reaction mechanisms & reagents"),
            Triple(66, Subject.MATHS, "Weak Area Rectification | Definite integrals & probability edge cases"),

            Triple(67, Subject.PHYSICS, "Full Mock Drill 2: High Yield Questions | Real examination speed drill"),
            Triple(67, Subject.CHEMISTRY, "Full Mock Drill 2: High Yield Questions | NCERT direct line questions"),
            Triple(67, Subject.MATHS, "Full Mock Drill 2: High Yield Questions | Shortcut methods & options elimination"),

            Triple(68, Subject.PHYSICS, "Grand Physics Formula Sheet Revision | 150 Formulas & Dimensions"),
            Triple(68, Subject.CHEMISTRY, "Grand Chemistry Reaction Sheet Revision | Catalysts & Conditions"),
            Triple(68, Subject.MATHS, "Grand Maths Theorem Sheet Revision | Identities & Conic conditions"),

            Triple(69, Subject.PHYSICS, "Final OMR Simulation: Full Mock Test 3 | Comprehensive 3-Hour Exam"),
            Triple(69, Subject.CHEMISTRY, "Final OMR Simulation: Chemistry Review | Question selection strategy"),
            Triple(69, Subject.MATHS, "Final OMR Simulation: Maths Strategy | NVT selection and time management"),

            Triple(70, Subject.PHYSICS, "Mindset, Time Management & Exam Hall Strategy | Calm, focus & confidence"),
            Triple(70, Subject.CHEMISTRY, "Final High-Weightage Reagent Quick Review | Quick 30-min glance"),
            Triple(70, Subject.MATHS, "Final Key Formulas & Short Notes Review | Ready for JEE Rank 1!")
        )

        daysSyllabus.forEachIndexed { index, (day, subject, detail) ->
            val parts = detail.split("|")
            val chapter = parts[0].trim()
            val concepts = if (parts.size > 1) parts[1].trim() else chapter
            val isHigh = day in listOf(4, 7, 10, 11, 15, 20, 22, 25, 28, 30, 34, 36, 45, 49, 51, 52, 56, 65, 69)
            val pyqs = if (isHigh) 35 else 25

            list.add(
                PlannerTopic(
                    id = "${subject.name.lowercase()}_day${day}_$index",
                    dayNumber = day,
                    subject = subject,
                    chapter = chapter,
                    topicTitle = chapter,
                    keyConcepts = concepts,
                    targetPYQs = pyqs,
                    isHighWeightage = isHigh
                )
            )
        }

        return list
    }
}
