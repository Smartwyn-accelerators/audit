export interface ITokenDetail {
  sub:string;
  exp?:string;
  scopes?:string[];
  id: number;
  name: string;

}