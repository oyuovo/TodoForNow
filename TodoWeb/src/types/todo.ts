export interface TodoItem {
  id: string;
  title: string;
  completed: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface TodoList {
  id: string;
  name: string;
  todos: TodoItem[];
}
