import type { DTOAlumno, DTOProfesor } from '$lib/generated/api';

interface EntityWithStatus {
	estado?: string;
	id?: number;
	alumnoEntreganteId?: number;
}

export interface Action {
	id: string;
	label: string;
	icon?: string;
	color?: string;
	condition: (user: DTOAlumno | DTOProfesor, entity?: EntityWithStatus) => boolean;
}

export class PermissionUtils {
	static canEditEntity(entityType: string, user: DTOAlumno | DTOProfesor): boolean {
		return (
			user.role.includes('ADMIN') || (entityType === 'profesores' && user.role.includes('PROFESOR'))
		);
	}

	static canDeleteEntity(entityType: string, user: DTOAlumno | DTOProfesor): boolean {
		return user.role.includes('ADMIN');
	}

	static canGradeDelivery(user: DTOAlumno | DTOProfesor): boolean {
		return user.role.includes('ADMIN') || user.role.includes('PROFESOR');
	}

	static canEnrollInClass(user: DTOAlumno | DTOProfesor): boolean {
		return user.role.includes('ALUMNO');
	}

	static canManagePayments(user: DTOAlumno | DTOProfesor): boolean {
		return user.role.includes('ADMIN');
	}

	static canViewAllDeliveries(user: DTOAlumno | DTOProfesor): boolean {
		return user.role.includes('ADMIN') || user.role.includes('PROFESOR');
	}

	static canViewAllStudents(user: DTOAlumno | DTOProfesor): boolean {
		return user.role.includes('ADMIN') || user.role.includes('PROFESOR');
	}

	static canViewAllTeachers(user: DTOAlumno | DTOProfesor): boolean {
		return user.role.includes('ADMIN');
	}

	static canCreateClasses(user: DTOAlumno | DTOProfesor): boolean {
		return user.role.includes('ADMIN') || user.role.includes('PROFESOR');
	}

	static canCreateExercises(user: DTOAlumno | DTOProfesor): boolean {
		return user.role.includes('ADMIN') || user.role.includes('PROFESOR');
	}

	static canCreateMaterials(user: DTOAlumno | DTOProfesor): boolean {
		return user.role.includes('ADMIN') || user.role.includes('PROFESOR');
	}

	static getVisibleActions(
		entityType: string,
		user: DTOAlumno | DTOProfesor,
		entity?: EntityWithStatus
	): Action[] {
		const actions: Action[] = [
			{
				id: 'view',
				label: 'Ver',
				icon: '👁️',
				color: 'blue',
				condition: () => true
			}
		];

		if (this.canEditEntity(entityType, user)) {
			actions.push({
				id: 'edit',
				label: 'Editar',
				icon: '✏️',
				color: 'green',
				condition: () => true
			});
		}

		if (this.canDeleteEntity(entityType, user)) {
			actions.push({
				id: 'delete',
				label: 'Eliminar',
				icon: '🗑️',
				color: 'red',
				condition: () => true
			});
		}

		// Special actions for deliveries
		if (entityType === 'entregas' && this.canGradeDelivery(user)) {
			actions.push({
				id: 'grade',
				label: 'Calificar',
				icon: '📝',
				color: 'purple',
				condition: (user, entity) => entity?.estado === 'ENTREGADO'
			});
		}

		return actions.filter((action) => action.condition(user, entity));
	}

	static canAccessRoute(route: string, user: DTOAlumno | DTOProfesor): boolean {
		const routePermissions: Record<string, string[]> = {
			'/admin': ['ADMIN'],
			'/profesores': ['ADMIN', 'PROFESOR'],
			'/alumnos': ['ADMIN', 'PROFESOR'],
			'/pagos/admin': ['ADMIN'],
			'/clases/nuevo': ['ADMIN', 'PROFESOR'],
			'/ejercicios/nuevo': ['ADMIN', 'PROFESOR'],
			'/materiales/nuevo': ['ADMIN', 'PROFESOR']
		};

		const requiredrole = routePermissions[route];
		if (!requiredrole) return true;

		return requiredrole.some((role) => user.role.includes(role));
	}

	static isAdmin(user: DTOAlumno | DTOProfesor): boolean {
		return user.role.includes('ADMIN');
	}

	static isProfessor(user: DTOAlumno | DTOProfesor): boolean {
		return user.role.includes('PROFESOR');
	}

	static isStudent(user: DTOAlumno | DTOProfesor): boolean {
		return user.role.includes('ALUMNO');
	}

	static canViewEntity(
		entityType: string,
		user: DTOAlumno | DTOProfesor,
		entity?: EntityWithStatus
	): boolean {
		// Students can only view their own entities
		if (this.isStudent(user)) {
			if (entityType === 'alumnos' && entity?.id !== user.id) {
				return false;
			}
			if (entityType === 'entregas' && entity?.alumnoEntreganteId !== user.id) {
				return false;
			}
		}

		return true;
	}
}
