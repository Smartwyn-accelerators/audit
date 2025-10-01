import { Time } from "@angular/common"

export interface TimesheetDetails {
    id?: string,
    timesheet: Timesheet,
    task: Task,
    timeoffType?: TimesheetTimeOffType,
    workDate?: Date,
    fileId?: string,
    hours: string,
    startTime: Time,
    endTime: Time
  }
  
  export interface Customer {
    customerId: string,
    name: string,
    description: string,
    isActive: boolean
  }
  
  export interface Project {
    projectId: string,
    name: string,
    description: string,
    startDate: Date,
    endDate: Date,
    customerDetails: Customer
  }
  
  export interface Task {
    taskId: string,
    name: string,
    description: string,
    status: boolean,
    projectDetails: Project,
    user: {
      id: number,
      emailAddress: string
    }
  }
  
  export interface Timesheet {
    id: string,
    userId: string,
    timesheetPeriod: TimesheetPeriod,
    timesheetStatus: TimesheetStatus,
    notes: string
  }
  
  export interface TimesheetPeriod {
    id?: number |string;
    startDate?: string;
    start?: Date;
    endDate?: string;
    end?: Date;
    status?: string
  }
  
  export interface TimesheetStatus {
    id?: number;
    statusName?: string
  }
  
  export interface TimesheetTimeOffType {
    id?: number;
    typeName?: string
  }
  