/** 0=非定时 1=定时未完成 3=定时已完成(不显示) */
export type Timeset = 0 | 1 | 3;

export interface TodoItem {
  id: string;
  title: string;
  completed: boolean;
  timeset: Timeset;
  createdAt: string;
  updatedAt: string;
}

export interface TodoList {
  id: string;
  name: string;
  todos: TodoItem[];
}
