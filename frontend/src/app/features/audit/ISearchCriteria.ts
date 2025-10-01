export enum operatorType {
    Contains = 'contains',
    Equals = 'equals',
    Range = 'range',
    NotEqual = 'notEqual',
  }


  export interface ISearchField {
    fieldName: any;
    searchValue?: string;
    startingValue?: string;
    endingValue?: string;
    operator: operatorType;
  }

  enum listProcessingType {
    Replace = "Replace",
    Append = "Append"
  }
  