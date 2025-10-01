export interface IAudit {
    id?: number,
    correlation?: string,
    path?: string,
    processTime?: number,
    requestTime?: string,
    responseTime?: string,
    responseStatus?: string,
    userName?: string,
    method?: string,
    contentType?: string,
    query?: string,
    scheme?: string,
    header?: string,
    body?: string,
    browser?: string,
    clientAddress?: string,
    response_status?: number,
    response?: string;
    changeType?:string;
      globalId?: {
        entity?: string;
        cdoId?: number;
      };
      commitMetadata?: {
        author?: string;
        properties?: any[];
        commitDate?: string;
        id?: number;
      };
      property?:string;
}