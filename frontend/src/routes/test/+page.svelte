<script lang="ts">
	import { onMount } from 'svelte';
	import { authStore } from '$lib/stores/authStore.svelte';
	import { goto } from '$app/navigation';
	import {
		classManagementApi,
		claseApi,
		autenticacionApi,
		alumnoApi,
		profesorApi,
		userOperationsApi
	} from '$lib/api';

	// Type definitions for error handling
	interface ApiErrorResponse {
		status: number;
		message: string;
		errorCode?: string;
		requiredRole?: string;
		currentUserRole?: string;
		resourceType?: string;
		action?: string;
		suggestion?: string;
		path?: string;
	}

	interface ApiError {
		response?: {
			status: number;
			data: ApiErrorResponse;
		};
		message?: string;
	}

	interface ClaseInscrita {
		id: number;
		titulo: string;
		descripcion: string;
		precio: number;
		nivel: string;
		presencialidad: string;
	}

	// Test state
	let loading = $state(false);
	let testResults = $state<string[]>([]);
	let loginLoading = $state(false);
	let dataLoading = $state(false);
	let accessDeniedLoading = $state(false);

	// Enhanced access denied testing
	let accessDeniedResults = $state<
		Array<{
			testName: string;
			status: 'success' | 'error' | 'info';
			message: string;
			errorDetails?: ApiErrorResponse | ApiError | string;
			timestamp: string;
		}>
	>([]);

	// Data state
	let students = $state<
		Array<{
			id?: number;
			firstName?: string;
			lastName?: string;
			email?: string;
			phoneNumber?: string;
			username?: string;
		}>
	>([]);
	let teachers = $state<
		Array<{
			id?: number;
			firstName?: string;
			lastName?: string;
			email?: string;
			specialty?: string;
			experience?: number;
		}>
	>([]);
	let classes = $state<
		Array<{
			id?: number;
			titulo?: string;
			descripcion?: string;
			nivel?: string;
			presencialidad?: string;
			precio?: number;
		}>
	>([]);

	// Check authentication
	$effect(() => {
		if (!authStore.isAuthenticated) {
			goto('/auth');
			return;
		}
	});

	// Reload classes when user role changes
	$effect(() => {
		if (authStore.isAuthenticated) {
			loadClasses();
		}
	});

	function addResult(message: string) {
		const timestamp = new Date().toLocaleTimeString();
		const uniqueMessage = `${timestamp}: ${message} (${Math.random().toString(36).substr(2, 9)})`;
		testResults = [...testResults, uniqueMessage];
		console.log(message);
	}

	function addAccessDeniedResult(
		testName: string,
		status: 'success' | 'error' | 'info',
		message: string,
		errorDetails?: ApiErrorResponse | ApiError | string
	) {
		const timestamp = new Date().toLocaleTimeString();
		accessDeniedResults = [
			...accessDeniedResults,
			{
				testName,
				status,
				message,
				errorDetails,
				timestamp
			}
		];
	}

	async function testEnhancedAccessDenied() {
		accessDeniedLoading = true;
		accessDeniedResults = [];

		try {
			addAccessDeniedResult('Test Setup', 'info', '🚀 Starting Enhanced Access Denied Testing...');

			const userRoles = authStore.user?.roles;
			const currentUser = authStore.user?.sub;

			addAccessDeniedResult(
				'User Info',
				'info',
				`Current user: ${currentUser}, Roles: ${userRoles}`
			);

			// Test 1: Try to create a course (requires ADMIN or PROFESOR)
			addAccessDeniedResult(
				'Test 1',
				'info',
				'📝 Testing course creation (requires ADMIN/PROFESOR)...'
			);
			try {
				await claseApi.crearCurso({
					dTOPeticionCrearCurso: {
						titulo: 'Test Course',
						descripcion: 'Test Description',
						nivel: 'INTERMEDIO',
						presencialidad: 'PRESENCIAL',
						precio: 100,
						fechaInicio: new Date('2025-02-01'),
						fechaFin: new Date('2025-06-01')
					}
				});
				addAccessDeniedResult(
					'Test 1',
					'success',
					'✅ Course creation successful (user has required permissions)'
				);
			} catch (error: unknown) {
				const apiError = error as ApiError;
				if (apiError.response?.status === 403) {
					const errorData = apiError.response.data;
					addAccessDeniedResult('Test 1', 'error', '❌ 403 Forbidden - Course creation denied', {
						status: errorData.status,
						message: errorData.message,
						errorCode: errorData.errorCode,
						requiredRole: errorData.requiredRole,
						currentUserRole: errorData.currentUserRole,
						resourceType: errorData.resourceType,
						action: errorData.action,
						suggestion: errorData.suggestion,
						path: errorData.path
					});
				} else {
					addAccessDeniedResult(
						'Test 1',
						'error',
						`❌ Unexpected error: ${String(error)}`,
						String(error)
					);
				}
			}

			// Test 2: Try to delete a class (requires ADMIN)
			addAccessDeniedResult('Test 2', 'info', '🗑️ Testing class deletion (requires ADMIN)...');
			try {
				// Use a test ID that likely doesn't exist
				await claseApi.borrarClasePorId({ id: 999999 });
				addAccessDeniedResult(
					'Test 2',
					'success',
					'✅ Class deletion successful (user has admin permissions)'
				);
			} catch (error: unknown) {
				const apiError = error as ApiError;
				if (apiError.response?.status === 403) {
					const errorData = apiError.response.data;
					addAccessDeniedResult('Test 2', 'error', '❌ 403 Forbidden - Class deletion denied', {
						status: errorData.status,
						message: errorData.message,
						errorCode: errorData.errorCode,
						requiredRole: errorData.requiredRole,
						currentUserRole: errorData.currentUserRole,
						resourceType: errorData.resourceType,
						action: errorData.action,
						suggestion: errorData.suggestion,
						path: errorData.path
					});
				} else if (apiError.response?.status === 404) {
					addAccessDeniedResult(
						'Test 2',
						'info',
						'ℹ️ 404 Not Found - Class not found (but deletion permission granted)'
					);
				} else {
					addAccessDeniedResult(
						'Test 2',
						'error',
						`❌ Unexpected error: ${String(error)}`,
						String(error)
					);
				}
			}

			// Test 3: Try to manage enrollments (requires ADMIN or PROFESOR)
			addAccessDeniedResult(
				'Test 3',
				'info',
				'👥 Testing enrollment management (requires ADMIN/PROFESOR)...'
			);
			try {
				// Try to enroll a student in a class (requires ADMIN or PROFESOR)
				await classManagementApi.inscribirAlumnoEnClase({
					claseId: 1,
					studentId: 1
				});
				addAccessDeniedResult(
					'Test 3',
					'success',
					'✅ Enrollment management successful (user has required permissions)'
				);
			} catch (error: unknown) {
				const apiError = error as ApiError;
				if (apiError.response?.status === 403) {
					const errorData = apiError.response.data;
					addAccessDeniedResult(
						'Test 3',
						'error',
						'❌ 403 Forbidden - Enrollment management denied',
						{
							status: errorData.status,
							message: errorData.message,
							errorCode: errorData.errorCode,
							requiredRole: errorData.requiredRole,
							currentUserRole: errorData.currentUserRole,
							resourceType: errorData.resourceType,
							action: errorData.action,
							suggestion: errorData.suggestion,
							path: errorData.path
						}
					);
				} else {
					addAccessDeniedResult(
						'Test 3',
						'error',
						`❌ Unexpected error: ${String(error)}`,
						String(error)
					);
				}
			}

			// Test 4: Try to access teacher-specific endpoint (requires PROFESOR)
			addAccessDeniedResult(
				'Test 4',
				'info',
				'👨‍🏫 Testing teacher-specific endpoint (requires PROFESOR)...'
			);
			try {
				await userOperationsApi.obtenerMisClases();
				addAccessDeniedResult(
					'Test 4',
					'success',
					'✅ Teacher endpoint successful (user has professor permissions)'
				);
			} catch (error: unknown) {
				const apiError = error as ApiError;
				if (apiError.response?.status === 403) {
					const errorData = apiError.response.data;
					addAccessDeniedResult('Test 4', 'error', '❌ 403 Forbidden - Teacher endpoint denied', {
						status: errorData.status,
						message: errorData.message,
						errorCode: errorData.errorCode,
						requiredRole: errorData.requiredRole,
						currentUserRole: errorData.currentUserRole,
						resourceType: errorData.resourceType,
						action: errorData.action,
						suggestion: errorData.suggestion,
						path: errorData.path
					});
				} else {
					addAccessDeniedResult(
						'Test 4',
						'error',
						`❌ Unexpected error: ${String(error)}`,
						String(error)
					);
				}
			}

			// Test 5: Try to access student-specific endpoint (requires ALUMNO)
			addAccessDeniedResult(
				'Test 5',
				'info',
				'👨‍🎓 Testing student-specific endpoint (requires ALUMNO)...'
			);
			try {
				await userOperationsApi.obtenerMisClasesInscritas();
				addAccessDeniedResult(
					'Test 5',
					'success',
					'✅ Student endpoint successful (user has student permissions)'
				);
			} catch (error: unknown) {
				const apiError = error as ApiError;
				if (apiError.response?.status === 403) {
					const errorData = apiError.response.data;
					addAccessDeniedResult('Test 5', 'error', '❌ 403 Forbidden - Student endpoint denied', {
						status: errorData.status,
						message: errorData.message,
						errorCode: errorData.errorCode,
						requiredRole: errorData.requiredRole,
						currentUserRole: errorData.currentUserRole,
						resourceType: errorData.resourceType,
						action: errorData.action,
						suggestion: errorData.suggestion,
						path: errorData.path
					});
				} else {
					addAccessDeniedResult(
						'Test 5',
						'error',
						`❌ Unexpected error: ${String(error)}`,
						String(error)
					);
				}
			}

			addAccessDeniedResult(
				'Test Complete',
				'info',
				'✅ Enhanced Access Denied Testing completed!'
			);
		} catch (error) {
			addAccessDeniedResult(
				'Test Error',
				'error',
				`❌ Test failed: ${String(error)}`,
				String(error)
			);
		} finally {
			accessDeniedLoading = false;
		}
	}

	async function clearAccessDeniedResults() {
		accessDeniedResults = [];
	}

	async function testEnrollmentAPI() {
		loading = true;
		testResults = [];

		try {
			addResult('🚀 Testing Enrollment API...');

			// Check user role first
			const userRoles = authStore.user?.roles;
			addResult(`🔐 Current user role: ${userRoles}`);

			if (!userRoles || !userRoles.includes('ROLE_ALUMNO')) {
				addResult('⚠️  WARNING: Enrollment endpoints require ROLE_ALUMNO (student role)');
				addResult(`     Current role: ${userRoles}`);
				addResult('     Expected 403 errors for non-student users');
			}

			// Test 1: Get all classes
			addResult('📚 Test 1: Getting all classes...');
			const allClassesResponse = await claseApi.obtenerClases();
			const allClasses = allClassesResponse.content || [];
			addResult(`     Found ${allClasses.length} classes`);

			if (allClasses.length === 0) {
				addResult('❌ No classes found. Cannot test enrollment.');
				return;
			}

			const testClass = allClasses[0];
			addResult(`     Using class: ${testClass.titulo} (ID: ${testClass.id})`);

			// Test 2: Check enrollment status
			addResult('🔍 Test 2: Checking enrollment status...');
			try {
				const status = await classManagementApi.obtenerMiInscripcion({ claseId: testClass.id! });
				addResult(`     ✅ Current status: ${status.isEnrolled ? 'Enrolled' : 'Not enrolled'}`);
			} catch (error) {
				if (String(error).includes('403')) {
					addResult(
						`     ❌ 403 Forbidden - User role ${userRoles} cannot check enrollment status`
					);
				} else {
					addResult(`     ❌ Status check failed: ${error}`);
				}
			}

			// Test 3: Try to enroll
			addResult('➕ Test 3: Trying to enroll...');
			try {
				const enrollResult = await classManagementApi.inscribirseEnClase({
					claseId: testClass.id!
				});
				addResult(`     ✅ Enrollment successful: ${enrollResult.className}`);
			} catch (error) {
				if (String(error).includes('403')) {
					addResult(`     ❌ 403 Forbidden - User role ${userRoles} cannot enroll in classes`);
				} else {
					addResult(`     ❌ Enrollment failed: ${error}`);
				}
			}

			// Test 4: Check status after enrollment
			addResult('🔍 Test 4: Checking status after enrollment...');
			try {
				const statusAfter = await classManagementApi.obtenerMiInscripcion({
					claseId: testClass.id!
				});
				addResult(
					`     ✅ Status after enrollment: ${statusAfter.isEnrolled ? 'Enrolled' : 'Not enrolled'}`
				);
			} catch (error) {
				if (String(error).includes('403')) {
					addResult(
						`     ❌ 403 Forbidden - User role ${userRoles} cannot check enrollment status`
					);
				} else {
					addResult(`     ❌ Status check failed: ${error}`);
				}
			}

			// Test 5: Try to unenroll
			addResult('➖ Test 5: Trying to unenroll...');
			try {
				const unenrollResult = await classManagementApi.darseDeBajaDeClase({
					claseId: testClass.id!
				});
				addResult(`     ✅ Unenrollment successful: ${unenrollResult.className}`);
			} catch (error) {
				if (String(error).includes('403')) {
					addResult(`     ❌ 403 Forbidden - User role ${userRoles} cannot unenroll from classes`);
				} else {
					addResult(`     ❌ Unenrollment failed: ${error}`);
				}
			}

			// Test 6: Final status check
			addResult('🔍 Test 6: Final status check...');
			try {
				const finalStatus = await classManagementApi.obtenerMiInscripcion({
					claseId: testClass.id!
				});
				addResult(`     ✅ Final status: ${finalStatus.isEnrolled ? 'Enrolled' : 'Not enrolled'}`);
			} catch (error) {
				if (String(error).includes('403')) {
					addResult(
						`     ❌ 403 Forbidden - User role ${userRoles} cannot check enrollment status`
					);
				} else {
					addResult(`     ❌ Final status check failed: ${error}`);
				}
			}

			if (!userRoles || !userRoles.includes('ROLE_ALUMNO')) {
				addResult(
					'💡 SOLUTION: Log in as a student (ROLE_ALUMNO) to test enrollment functionality'
				);
			} else {
				addResult('✅ Enrollment API tests completed!');
			}
		} catch (error) {
			addResult(`❌ Test failed: ${error}`);
			console.error('Test error:', error);
		} finally {
			loading = false;
		}
	}

	async function testSimpleAPI() {
		loading = true;
		testResults = [];

		try {
			addResult('🧪 Testing simple API calls...');

			// Test basic API connectivity
			addResult('🌐 Test 1: Testing API connectivity...');
			const classesResponse = await claseApi.obtenerClases();
			const classes = classesResponse.content || [];
			addResult(`     API is working. Found ${classes.length} classes.`);

			// Test authentication
			addResult('🔐 Test 2: Testing authentication...');
			addResult(`     Token present: ${authStore.token ? 'Yes' : 'No'}`);
			addResult(`     User: ${authStore.user?.sub || 'None'}`);
			addResult(`     Roles: ${authStore.user?.roles || 'None'}`);

			addResult('✅ Simple API tests completed!');
		} catch (error) {
			addResult(`❌ Simple test failed: ${error}`);
			console.error('Simple test error:', error);
		} finally {
			loading = false;
		}
	}

	async function quickLogin(username: string, password: string, role: string) {
		loginLoading = true;
		addResult(`🔄 Logging in as ${role}...`);

		try {
			// First logout current user
			authStore.logout();
			addResult(`     Logged out current user`);

			// Then login with new credentials
			const loginResponse = await autenticacionApi.login({
				dTOPeticionLogin: {
					username,
					password
				}
			});

			// Store the authentication data
			authStore.login(loginResponse.token);
			addResult(`     ✅ Successfully logged in as ${role}: ${username}`);
			addResult(`     Token received and stored`);

			// Redirect to test page after successful login
			setTimeout(() => {
				goto('/test');
			}, 100); // Small delay to show the success message
		} catch (error) {
			addResult(`     ❌ Login failed for ${role}: ${error}`);
			console.error('Login error:', error);
		} finally {
			loginLoading = false;
		}
	}

	async function loginAsAdmin() {
		await quickLogin('admin', 'admin', 'Admin');
	}

	async function loginAsStudent() {
		await quickLogin('estudiante', 'password', 'Student');
	}

	async function loginAsProfessor() {
		await quickLogin('profesor', 'password', 'Professor');
	}

	async function loginAsSpecificStudent(username: string, studentName: string) {
		loginLoading = true;
		addResult(`🔄 Logging in as student: ${studentName}...`);

		try {
			// First logout current user
			authStore.logout();
			addResult(`     Logged out current user`);

			// Then login with student credentials
			const loginResponse = await autenticacionApi.login({
				dTOPeticionLogin: {
					username,
					password: 'password' // All students use "password"
				}
			});

			// Store the authentication data
			authStore.login(loginResponse.token);
			addResult(`     ✅ Successfully logged in as student: ${studentName}`);
			addResult(`     Token received and stored`);

			// Redirect to test page after successful login
			setTimeout(() => {
				goto('/test');
			}, 100); // Small delay to show the success message
		} catch (error) {
			addResult(`     ❌ Login failed for student ${studentName}: ${error}`);
			console.error('Login error:', error);
		} finally {
			loginLoading = false;
		}
	}

	async function loadSystemData() {
		dataLoading = true;

		try {
			// Load students
			const studentsResponse = await alumnoApi.obtenerAlumnos({
				page: 0,
				size: 10
			});
			students = studentsResponse.content || [];

			// Load teachers
			const teachersResponse = await profesorApi.obtenerProfesores({
				page: 0,
				size: 10
			});
			teachers = teachersResponse.content || [];

			// Load classes - different based on user role
			await loadClasses();
		} catch (error) {
			console.error('Error loading system data:', error);
		} finally {
			dataLoading = false;
		}
	}

	async function loadClasses() {
		try {
			const userRoles = authStore.user?.roles;

			if (userRoles && userRoles.includes('ROLE_ALUMNO')) {
				// If student, load their enrolled classes
				const enrolledClasses = await userOperationsApi.obtenerMisClasesInscritas();
				// Convert DTOClaseInscrita to DTOClase format
				classes = (enrolledClasses as ClaseInscrita[])
					.slice(0, 10)
					.map((claseInscrita: ClaseInscrita) => ({
						id: claseInscrita.id,
						titulo: claseInscrita.titulo,
						descripcion: claseInscrita.descripcion,
						precio: claseInscrita.precio,
						nivel: claseInscrita.nivel,
						presencialidad: claseInscrita.presencialidad
					}));
			} else {
				// If admin/professor, load all classes
				const classesResponse = await claseApi.obtenerClases();
				classes = (classesResponse.content || []).slice(0, 10);
			}
		} catch (error) {
			console.error('Error loading classes:', error);
			// Fallback to empty array if there's an error (e.g., no enrolled classes)
			classes = [];
		}
	}

	onMount(() => {
		addResult('🔧 Test page loaded. Ready to run tests.');
		loadSystemData();
	});
</script>

<svelte:head>
	<title>API Test Page - Academia</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	<!-- Header -->
	<div class="mb-6">
		<h1 class="text-3xl font-bold text-gray-900">🧪 API Test Page</h1>
		<p class="text-gray-600">Testing enrollment functionality and API connectivity</p>
	</div>

	<!-- Authentication Status -->
	<div class="mb-6 rounded-lg border border-blue-200 bg-blue-50 p-4">
		<h2 class="mb-2 text-lg font-semibold text-blue-900">Authentication Status</h2>
		<div class="text-sm text-blue-800">
			<p><strong>Authenticated:</strong> {authStore.isAuthenticated ? '✅ Yes' : '❌ No'}</p>
			<p><strong>User:</strong> {authStore.user?.sub || 'N/A'}</p>
			<p><strong>Roles:</strong> {authStore.user?.roles || 'N/A'}</p>
			<p><strong>Token:</strong> {authStore.token ? '✅ Present' : '❌ Missing'}</p>
		</div>
	</div>

	<!-- Quick Login Controls -->
	<div class="mb-6 rounded-lg border border-purple-200 bg-purple-50 p-4">
		<h2 class="mb-3 text-lg font-semibold text-purple-900">🚀 Quick Login (for Testing)</h2>
		<div class="flex flex-wrap gap-3">
			<button
				onclick={loginAsAdmin}
				disabled={loginLoading || loading}
				class="rounded-lg bg-red-600 px-4 py-2 font-semibold text-white hover:bg-red-700 disabled:cursor-not-allowed disabled:opacity-50"
			>
				{loginLoading ? '🔄 Logging in...' : '👨‍💼 Login as Admin'}
			</button>

			<button
				onclick={loginAsStudent}
				disabled={loginLoading || loading}
				class="rounded-lg bg-green-600 px-4 py-2 font-semibold text-white hover:bg-green-700 disabled:cursor-not-allowed disabled:opacity-50"
			>
				{loginLoading ? '🔄 Logging in...' : '👨‍🎓 Login as Student'}
			</button>

			<button
				onclick={loginAsProfessor}
				disabled={loginLoading || loading}
				class="rounded-lg bg-blue-600 px-4 py-2 font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
			>
				{loginLoading ? '🔄 Logging in...' : '👨‍🏫 Login as Professor'}
			</button>
		</div>
		<p class="mt-2 text-sm text-purple-700">
			Credentials: admin/admin, estudiante/password, profesor/password
		</p>
	</div>

	<!-- Test Controls -->
	<div class="mb-6 flex gap-4">
		<button
			onclick={testSimpleAPI}
			disabled={loading || loginLoading}
			class="rounded-lg bg-blue-600 px-6 py-3 font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
		>
			{loading ? '🔄 Running...' : '🧪 Test Simple API'}
		</button>

		<button
			onclick={testEnrollmentAPI}
			disabled={loading || loginLoading}
			class="rounded-lg bg-green-600 px-6 py-3 font-semibold text-white hover:bg-green-700 disabled:cursor-not-allowed disabled:opacity-50"
		>
			{loading ? '🔄 Running...' : '🎯 Test Enrollment API'}
		</button>

		<button
			onclick={testEnhancedAccessDenied}
			disabled={accessDeniedLoading || loginLoading}
			class="rounded-lg bg-red-600 px-6 py-3 font-semibold text-white hover:bg-red-700 disabled:cursor-not-allowed disabled:opacity-50"
		>
			{accessDeniedLoading ? '🔄 Running...' : '🚫 Test Access Denied'}
		</button>

		<button
			onclick={loadSystemData}
			disabled={dataLoading}
			class="rounded-lg bg-orange-600 px-6 py-3 font-semibold text-white hover:bg-orange-700 disabled:cursor-not-allowed disabled:opacity-50"
		>
			{dataLoading ? '🔄 Loading...' : '🔄 Refresh Data'}
		</button>

		<button
			onclick={() => goto('/')}
			class="rounded-lg bg-gray-600 px-6 py-3 font-semibold text-white hover:bg-gray-700"
		>
			🏠 Go Home
		</button>
	</div>

	<!-- System Data Overview -->
	<div class="mb-6 grid grid-cols-1 gap-6 lg:grid-cols-3">
		<!-- Students Section -->
		<div class="rounded-lg bg-white shadow-lg">
			<div class="border-b border-gray-200 bg-green-50 p-4">
				<h2 class="text-lg font-semibold text-green-900">👨‍🎓 Students ({students.length})</h2>
			</div>
			<div class="max-h-96 overflow-y-auto p-4">
				{#if dataLoading}
					<div class="py-4 text-center">
						<div
							class="mx-auto h-8 w-8 animate-spin rounded-full border-b-2 border-green-600"
						></div>
						<p class="mt-2 text-sm text-gray-600">Loading students...</p>
					</div>
				{:else if students.length === 0}
					<div class="py-8 text-center text-gray-500">
						<p>No students found</p>
					</div>
				{:else}
					<div class="space-y-2">
						{#each students as student (student.id)}
							<div class="rounded-lg border p-3 hover:bg-gray-50">
								<div class="flex items-start justify-between">
									<div class="flex-1">
										<h4 class="font-medium text-gray-900">
											{student.firstName}
											{student.lastName}
										</h4>
										<p class="text-sm text-gray-600">{student.email}</p>
										{#if student.phoneNumber}
											<p class="text-xs text-gray-500">{student.phoneNumber}</p>
										{/if}
									</div>
									<div class="flex flex-col items-end gap-2">
										<span class="rounded bg-green-100 px-2 py-1 text-xs text-green-800">
											ID: {student.id}
										</span>
										{#if student.username}
											<button
												onclick={() =>
													loginAsSpecificStudent(
														student.username!,
														`${student.firstName} ${student.lastName}`
													)}
												disabled={loginLoading}
												class="rounded bg-green-600 px-2 py-1 text-xs font-medium text-white hover:bg-green-700 disabled:cursor-not-allowed disabled:opacity-50"
											>
												{loginLoading ? '🔄' : '👤 Log in as'}
											</button>
										{/if}
									</div>
								</div>
							</div>
						{/each}
					</div>
				{/if}
			</div>
		</div>

		<!-- Teachers Section -->
		<div class="rounded-lg bg-white shadow-lg">
			<div class="border-b border-gray-200 bg-blue-50 p-4">
				<h2 class="text-lg font-semibold text-blue-900">👨‍🏫 Teachers ({teachers.length})</h2>
			</div>
			<div class="max-h-96 overflow-y-auto p-4">
				{#if dataLoading}
					<div class="py-4 text-center">
						<div class="mx-auto h-8 w-8 animate-spin rounded-full border-b-2 border-blue-600"></div>
						<p class="mt-2 text-sm text-gray-600">Loading teachers...</p>
					</div>
				{:else if teachers.length === 0}
					<div class="py-8 text-center text-gray-500">
						<p>No teachers found</p>
					</div>
				{:else}
					<div class="space-y-2">
						{#each teachers as teacher (teacher.id)}
							<div class="rounded-lg border p-3 hover:bg-gray-50">
								<div class="flex items-start justify-between">
									<div>
										<h4 class="font-medium text-gray-900">
											{teacher.firstName}
											{teacher.lastName}
										</h4>
										<p class="text-sm text-gray-600">{teacher.email}</p>
										{#if teacher.specialty}
											<p class="text-xs font-medium text-blue-600">{teacher.specialty}</p>
										{/if}
										{#if teacher.experience}
											<p class="text-xs text-gray-500">{teacher.experience} años exp.</p>
										{/if}
									</div>
									<span class="rounded bg-blue-100 px-2 py-1 text-xs text-blue-800">
										ID: {teacher.id}
									</span>
								</div>
							</div>
						{/each}
					</div>
				{/if}
			</div>
		</div>

		<!-- Classes Section -->
		<div class="rounded-lg bg-white shadow-lg">
			<div class="border-b border-gray-200 bg-purple-50 p-4">
				<h2 class="text-lg font-semibold text-purple-900">
					📚 {authStore.user?.roles?.includes('ROLE_ALUMNO')
						? 'My Enrolled Classes'
						: 'All Classes'} ({classes.length})
				</h2>
			</div>
			<div class="max-h-96 overflow-y-auto p-4">
				{#if dataLoading}
					<div class="py-4 text-center">
						<div
							class="mx-auto h-8 w-8 animate-spin rounded-full border-b-2 border-purple-600"
						></div>
						<p class="mt-2 text-sm text-gray-600">Loading classes...</p>
					</div>
				{:else if classes.length === 0}
					<div class="py-8 text-center text-gray-500">
						{#if authStore.user?.roles?.includes('ROLE_ALUMNO')}
							<p>No enrolled classes</p>
							<p class="mt-1 text-xs">This student is not enrolled in any classes yet</p>
						{:else}
							<p>No classes found</p>
						{/if}
					</div>
				{:else}
					<div class="space-y-2">
						{#each classes as clase (clase.id)}
							<div class="rounded-lg border p-3 hover:bg-gray-50">
								<div class="flex items-start justify-between">
									<div>
										<h4 class="font-medium text-gray-900">
											{clase.titulo}
										</h4>
										{#if clase.descripcion}
											<p class="line-clamp-2 text-sm text-gray-600">{clase.descripcion}</p>
										{/if}
										<div class="mt-1 flex gap-2">
											{#if clase.nivel}
												<span class="rounded bg-yellow-100 px-2 py-1 text-xs text-yellow-800">
													{clase.nivel}
												</span>
											{/if}
											{#if clase.presencialidad}
												<span class="rounded bg-gray-100 px-2 py-1 text-xs text-gray-800">
													{clase.presencialidad}
												</span>
											{/if}
										</div>
										{#if clase.precio}
											<p class="mt-1 text-sm font-medium text-green-600">€{clase.precio}</p>
										{/if}
									</div>
									<span class="rounded bg-purple-100 px-2 py-1 text-xs text-purple-800">
										ID: {clase.id}
									</span>
								</div>
							</div>
						{/each}
					</div>
				{/if}
			</div>
		</div>
	</div>

	<!-- Enhanced Access Denied Results -->
	<div class="mb-6 rounded-lg bg-white shadow-lg">
		<div class="border-b border-gray-200 p-4">
			<div class="flex items-center justify-between">
				<div>
					<h2 class="text-lg font-semibold text-gray-900">🚫 Enhanced Access Denied Testing</h2>
					<p class="text-sm text-gray-600">Detailed access denied response analysis</p>
				</div>
				<button
					onclick={clearAccessDeniedResults}
					disabled={accessDeniedResults.length === 0}
					class="rounded bg-gray-100 px-3 py-1 text-sm font-medium text-gray-700 hover:bg-gray-200 disabled:cursor-not-allowed disabled:opacity-50"
				>
					Clear Results
				</button>
			</div>
		</div>

		<div class="max-h-96 overflow-y-auto p-4">
			{#if accessDeniedResults.length === 0}
				<div class="py-8 text-center text-gray-500">
					<p>No access denied tests run yet.</p>
					<p class="mt-1 text-sm">
						Click "Test Access Denied" to start testing enhanced error responses.
					</p>
				</div>
			{:else}
				<div class="space-y-4">
					{#each accessDeniedResults as result (result.timestamp + result.testName)}
						<div
							class="rounded-lg border p-4 {result.status === 'success'
								? 'border-green-200 bg-green-50'
								: result.status === 'error'
									? 'border-red-200 bg-red-50'
									: 'border-blue-200 bg-blue-50'}"
						>
							<div class="flex items-start justify-between">
								<div class="flex-1">
									<div class="mb-2 flex items-center gap-2">
										<span class="font-semibold text-gray-900">{result.testName}</span>
										<span class="text-xs text-gray-500">{result.timestamp}</span>
									</div>
									<p
										class="text-sm {result.status === 'success'
											? 'text-green-800'
											: result.status === 'error'
												? 'text-red-800'
												: 'text-blue-800'}"
									>
										{result.message}
									</p>

									{#if result.errorDetails && typeof result.errorDetails === 'object'}
										<div class="mt-3 rounded border bg-white p-3">
											<h4 class="mb-2 text-xs font-semibold text-gray-700">Error Details:</h4>
											<div class="space-y-1 text-xs">
												{#if (result.errorDetails as ApiErrorResponse).status}
													<div>
														<span class="font-medium">Status:</span>
														{(result.errorDetails as ApiErrorResponse).status}
													</div>
												{/if}
												{#if (result.errorDetails as ApiErrorResponse).message}
													<div>
														<span class="font-medium">Message:</span>
														{(result.errorDetails as ApiErrorResponse).message}
													</div>
												{/if}
												{#if (result.errorDetails as ApiErrorResponse).errorCode}
													<div>
														<span class="font-medium">Error Code:</span>
														<code class="rounded bg-gray-100 px-1"
															>{(result.errorDetails as ApiErrorResponse).errorCode}</code
														>
													</div>
												{/if}
												{#if (result.errorDetails as ApiErrorResponse).requiredRole}
													<div>
														<span class="font-medium">Required Role:</span>
														<span class="rounded bg-yellow-100 px-1"
															>{(result.errorDetails as ApiErrorResponse).requiredRole}</span
														>
													</div>
												{/if}
												{#if (result.errorDetails as ApiErrorResponse).currentUserRole}
													<div>
														<span class="font-medium">Current Role:</span>
														<span class="rounded bg-blue-100 px-1"
															>{(result.errorDetails as ApiErrorResponse).currentUserRole}</span
														>
													</div>
												{/if}
												{#if (result.errorDetails as ApiErrorResponse).resourceType}
													<div>
														<span class="font-medium">Resource:</span>
														<code class="rounded bg-gray-100 px-1"
															>{(result.errorDetails as ApiErrorResponse).resourceType}</code
														>
													</div>
												{/if}
												{#if (result.errorDetails as ApiErrorResponse).action}
													<div>
														<span class="font-medium">Action:</span>
														<code class="rounded bg-gray-100 px-1"
															>{(result.errorDetails as ApiErrorResponse).action}</code
														>
													</div>
												{/if}
												{#if (result.errorDetails as ApiErrorResponse).path}
													<div>
														<span class="font-medium">Path:</span>
														<code class="rounded bg-gray-100 px-1"
															>{(result.errorDetails as ApiErrorResponse).path}</code
														>
													</div>
												{/if}
												{#if (result.errorDetails as ApiErrorResponse).suggestion}
													<div class="mt-2 rounded border-l-4 border-blue-400 bg-blue-50 p-2">
														<span class="font-medium text-blue-800">Suggestion:</span>
														<p class="mt-1 text-blue-700">
															{(result.errorDetails as ApiErrorResponse).suggestion}
														</p>
													</div>
												{/if}
											</div>
										</div>
									{:else if result.errorDetails}
										<div class="mt-3 rounded border bg-white p-3">
											<h4 class="mb-2 text-xs font-semibold text-gray-700">Error Details:</h4>
											<div class="text-xs text-gray-600">
												{String(result.errorDetails)}
											</div>
										</div>
									{/if}
								</div>
							</div>
						</div>
					{/each}
				</div>
			{/if}
		</div>
	</div>

	<!-- Test Results -->
	<div class="rounded-lg bg-white shadow-lg">
		<div class="border-b border-gray-200 p-4">
			<h2 class="text-lg font-semibold text-gray-900">Test Results</h2>
			<p class="text-sm text-gray-600">Real-time test execution log</p>
		</div>

		<div class="max-h-96 overflow-y-auto p-4">
			{#if testResults.length === 0}
				<div class="py-8 text-center text-gray-500">
					<p>No tests run yet. Click a test button to start.</p>
				</div>
			{:else}
				<div class="space-y-1">
					{#each testResults as result (result)}
						<div
							class="rounded p-2 font-mono text-sm {result.includes('✅')
								? 'bg-green-50 text-green-800'
								: result.includes('❌')
									? 'bg-red-50 text-red-800'
									: 'bg-gray-50 text-gray-800'}"
						>
							{result}
						</div>
					{/each}
				</div>
			{/if}
		</div>
	</div>
</div>
