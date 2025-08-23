<script module lang="ts">
	import { defineMeta } from '@storybook/addon-svelte-csf';
	import EntityDataTable from '../lib/components/common/EntityDataTable.svelte';
	import { fn, expect, userEvent, within } from 'storybook/test';
	import type {
		PaginatedEntities,
		EntityColumn,
		EntityAction,
		EntityPagination
	} from '../lib/components/common/types';

	// Mock data for testing
	const mockAlumnosData: PaginatedEntities<Record<string, unknown>> = {
		content: [
			{
				id: 1,
				nombre: 'Juan',
				apellidos: 'García López',
				email: 'juan@email.com',
				dni: '12345678A',
				matriculado: true,
				fechaNacimiento: '1995-03-15'
			},
			{
				id: 2,
				nombre: 'María',
				apellidos: 'Rodríguez Sánchez',
				email: 'maria@email.com',
				dni: '87654321B',
				matriculado: true,
				fechaNacimiento: '1992-07-22'
			},
			{
				id: 3,
				nombre: 'Carlos',
				apellidos: 'Ruiz Martínez',
				email: 'carlos@email.com',
				dni: '11223344C',
				matriculado: false,
				fechaNacimiento: '1998-11-08'
			}
		],
		page: {
			totalElements: 3,
			totalPages: 1,
			size: 20,
			number: 0,
			first: true,
			last: true,
			hasNext: false,
			hasPrevious: false
		}
	};

	const mockClasesData: PaginatedEntities<Record<string, unknown>> = {
		content: [
			{
				id: 1,
				titulo: 'Matemáticas Avanzadas',
				descripcion: 'Curso de matemáticas para nivel avanzado',
				nivel: 'AVANZADO',
				presencialidad: 'PRESENCIAL',
				precio: 150,
				profesor: 'Dr. García'
			},
			{
				id: 2,
				titulo: 'Programación Web',
				descripcion: 'Aprende desarrollo web moderno',
				nivel: 'INTERMEDIO',
				presencialidad: 'ONLINE',
				precio: 120,
				profesor: 'Ing. López'
			},
			{
				id: 3,
				titulo: 'Inglés Básico',
				descripcion: 'Curso de inglés para principiantes',
				nivel: 'PRINCIPIANTE',
				presencialidad: 'HIBRIDA',
				precio: 80,
				profesor: 'Prof. Smith'
			}
		],
		page: {
			totalElements: 3,
			totalPages: 1,
			size: 20,
			number: 0,
			first: true,
			last: true,
			hasNext: false,
			hasPrevious: false
		}
	};

	// Column configurations
	const alumnosColumns: EntityColumn[] = [
		{ key: 'id', header: 'ID', sortable: true },
		{ key: 'nombre', header: 'Nombre', sortable: true },
		{ key: 'apellidos', header: 'Apellidos', sortable: true },
		{ key: 'email', header: 'Email', sortable: true },
		{ key: 'dni', header: 'DNI', sortable: true },
		{
			key: 'matriculado',
			header: 'Matriculado',
			sortable: true,
			class: 'text-center'
		},
		{ key: 'fechaNacimiento', header: 'Fecha de Nacimiento', sortable: true }
	];

	const clasesColumns: EntityColumn[] = [
		{ key: 'id', header: 'ID', sortable: true },
		{ key: 'titulo', header: 'Título', sortable: true },
		{ key: 'descripcion', header: 'Descripción', sortable: true },
		{ key: 'nivel', header: 'Nivel', sortable: true },
		{ key: 'presencialidad', header: 'Presencialidad', sortable: true },
		{ key: 'precio', header: 'Precio', sortable: true },
		{ key: 'profesor', header: 'Profesor', sortable: true }
	];

	// Action configurations
	const alumnosActions: EntityAction[] = [
		{
			label: 'Ver',
			icon: '👁️',
			color: 'blue',
			hoverColor: 'blue',
			action: fn()
		},
		{
			label: 'Editar',
			icon: '✏️',
			color: 'green',
			hoverColor: 'green',
			action: fn()
		},
		{
			label: 'Eliminar',
			icon: '🗑️',
			color: 'red',
			hoverColor: 'red',
			action: fn()
		}
	];

	const clasesActions: EntityAction[] = [
		{
			label: 'Ver',
			icon: '👁️',
			color: 'blue',
			hoverColor: 'blue',
			action: fn()
		},
		{
			label: 'Editar',
			icon: '✏️',
			color: 'green',
			hoverColor: 'green',
			action: fn()
		},
		{
			label: 'Inscribir',
			icon: '📝',
			color: 'purple',
			hoverColor: 'purple',
			action: fn()
		}
	];

	// Default pagination
	const defaultPagination: EntityPagination = {
		page: 0,
		size: 20,
		sortBy: 'id',
		sortDirection: 'ASC'
	};

	const { Story } = defineMeta({
		title: 'Components/EntityDataTable',
		component: EntityDataTable,
		tags: ['autodocs'],
		argTypes: {
			loading: { control: 'boolean' },
			showMobileView: { control: 'boolean' }
		}
	});
</script>

<!-- Alumnos Table -->
<Story
	name="Alumnos Table"
	args={{
		paginatedData: mockAlumnosData,
		columns: alumnosColumns,
		actions: alumnosActions,
		loading: false,
		currentPagination: defaultPagination,
		entityName: 'alumno',
		entityNamePlural: 'alumnos',
		showMobileView: true
	}}
/>

<!-- Clases Table -->
<Story
	name="Clases Table"
	args={{
		paginatedData: mockClasesData,
		columns: clasesColumns,
		actions: clasesActions,
		loading: false,
		currentPagination: defaultPagination,
		entityName: 'clase',
		entityNamePlural: 'clases',
		showMobileView: true
	}}
/>

<!-- Loading State -->
<Story
	name="Loading State"
	args={{
		paginatedData: null,
		columns: alumnosColumns,
		actions: alumnosActions,
		loading: true,
		currentPagination: defaultPagination,
		entityName: 'alumno',
		entityNamePlural: 'alumnos',
		showMobileView: true
	}}
/>

<!-- Empty Results -->
<Story
	name="Empty Results"
	args={{
		paginatedData: {
			content: [],
			page: {
				totalElements: 0,
				totalPages: 0,
				size: 20,
				number: 0,
				first: true,
				last: true,
				hasNext: false,
				hasPrevious: false
			}
		},
		columns: alumnosColumns,
		actions: alumnosActions,
		loading: false,
		currentPagination: defaultPagination,
		entityName: 'alumno',
		entityNamePlural: 'alumnos',
		showMobileView: true
	}}
/>

<!-- No Actions -->
<Story
	name="No Actions"
	args={{
		paginatedData: mockAlumnosData,
		columns: alumnosColumns,
		actions: [],
		loading: false,
		currentPagination: defaultPagination,
		entityName: 'alumno',
		entityNamePlural: 'alumnos',
		showMobileView: true
	}}
/>

<!-- Read-only Table -->
<Story 
	name="Read-only Table" 
	args={{
		paginatedData: mockClasesData,
		columns: clasesColumns,
		actions: [
			{
				label: 'Ver',
				icon: '👁️',
				color: 'blue',
				hoverColor: 'blue',
				action: fn()
			}
		],
		loading: false,
		currentPagination: defaultPagination,
		entityName: 'clase',
		entityNamePlural: 'clases',
		showMobileView: true
	}} 
/>

<!-- Interactive Table Testing -->
<Story 
	name="Interactive Table Testing"
	args={{
		paginatedData: mockAlumnosData,
		columns: alumnosColumns,
		actions: alumnosActions,
		loading: false,
		currentPagination: defaultPagination,
		entityName: 'alumno',
		entityNamePlural: 'alumnos',
		showMobileView: true
	}}
	play={async ({ args, canvasElement }) => {
		const canvas = within(canvasElement);
		
		// Test 1: Check that table renders with data
		const table = canvas.getByRole('table');
		expect(table).toBeInTheDocument();
		
		// Test 2: Check that headers are sortable
		const nameHeader = canvas.getByText('Nombre');
		await userEvent.click(nameHeader);
		
		// Test 3: Check action buttons
		const viewButtons = canvas.getAllByTitle('Ver');
		expect(viewButtons).toHaveLength(3); // Should have 3 view buttons for 3 rows
		
		// Test 4: Click on action buttons
		await userEvent.click(viewButtons[0]);
		expect(args.actions[0].action).toHaveBeenCalled();
		
		// Test 5: Check that all data is displayed
		expect(canvas.getByText('Juan')).toBeInTheDocument();
		expect(canvas.getByText('María')).toBeInTheDocument();
		expect(canvas.getByText('Carlos')).toBeInTheDocument();
		
		// Test 6: Check action buttons work for different rows
		const editButtons = canvas.getAllByTitle('Editar');
		await userEvent.click(editButtons[1]); // Click edit on second row
		expect(args.actions[1].action).toHaveBeenCalled();
	}}
/>

<!-- Sorting Functionality Test -->
<Story 
	name="Sorting Functionality Test"
	args={{
		paginatedData: mockAlumnosData,
		columns: alumnosColumns,
		actions: [],
		loading: false,
		currentPagination: defaultPagination,
		entityName: 'alumno',
		entityNamePlural: 'alumnos',
		showMobileView: true,
		onSort: fn()
	}}
	play={async ({ args, canvasElement }) => {
		const canvas = within(canvasElement);
		
		// Test sorting by different columns
		const headers = ['ID', 'Nombre', 'Apellidos', 'Email', 'DNI'];
		
		for (const headerText of headers) {
			const header = canvas.getByText(headerText);
			await userEvent.click(header);
			// Verify sort function was called
			expect(args.onSort).toHaveBeenCalled();
		}
		
		// Test sort direction indicator
		const nameHeader = canvas.getByText('Nombre');
		await userEvent.click(nameHeader);
		
		// Should see sort indicators (↑ or ↓)
		const sortIndicators = canvas.container.querySelectorAll('[class*="text-blue-600"]');
		expect(sortIndicators.length).toBeGreaterThan(0);
	}}
/>

<!-- Action Button Validation Test -->
<Story 
	name="Action Button Validation Test"
	args={{
		paginatedData: {
			content: [
				{ 
					id: 1, 
					nombre: 'Juan', 
					apellidos: 'García López', 
					email: 'juan@email.com', 
					dni: '12345678A',
					matriculado: true,
					canEdit: true,
					canDelete: false
				},
				{ 
					id: 2, 
					nombre: 'María', 
					apellidos: 'Rodríguez Sánchez', 
					email: 'maria@email.com', 
					dni: '87654321B',
					matriculado: true,
					canEdit: false,
					canDelete: true
				}
			],
			page: {
				totalElements: 2,
				totalPages: 1,
				size: 20,
				number: 0,
				first: true,
				last: true,
				hasNext: false,
				hasPrevious: false
			}
		},
		columns: alumnosColumns,
		actions: [
			{
				label: 'Ver',
				icon: '👁️',
				color: 'blue',
				hoverColor: 'blue',
				action: fn()
			},
			{
				label: 'Editar',
				icon: '✏️',
				color: 'green',
				hoverColor: 'green',
				condition: (entity) => entity.canEdit === true,
				action: fn()
			},
			{
				label: 'Eliminar',
				icon: '🗑️',
				color: 'red',
				hoverColor: 'red',
				condition: (entity) => entity.canDelete === true,
				action: fn()
			}
		],
		loading: false,
		currentPagination: defaultPagination,
		entityName: 'alumno',
		entityNamePlural: 'alumnos',
		showMobileView: true
	}}
	play={async ({ args, canvasElement }) => {
		const canvas = within(canvasElement);
		
		// Test conditional actions - Juan can edit but not delete
		const rows = canvas.getAllByRole('row');
		const juanRow = rows[1]; // First data row (header is row 0)
		
		// Juan should have View and Edit buttons
		const juanButtons = within(juanRow).getAllByRole('button');
		expect(juanButtons).toHaveLength(2); // View + Edit
		
		// María should have View and Delete buttons  
		const mariaRow = rows[2];
		const mariaButtons = within(mariaRow).getAllByRole('button');
		expect(mariaButtons).toHaveLength(2); // View + Delete
		
		// Test that edit button only appears for Juan
		expect(within(juanRow).queryByTitle('Editar')).toBeInTheDocument();
		expect(within(mariaRow).queryByTitle('Editar')).not.toBeInTheDocument();
		
		// Test that delete button only appears for María
		expect(within(juanRow).queryByTitle('Eliminar')).not.toBeInTheDocument();
		expect(within(mariaRow).queryByTitle('Eliminar')).toBeInTheDocument();
	}}
/>
