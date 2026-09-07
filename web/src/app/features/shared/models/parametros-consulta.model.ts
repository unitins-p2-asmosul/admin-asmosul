export interface ParametrosConsulta {
  page: number;
  size: number;
  sort?: string; // Formato esperado pelo Spring: "campo,asc" ou "campo,desc"
  incluirInativos?: boolean;
  filtro?: string;
}
