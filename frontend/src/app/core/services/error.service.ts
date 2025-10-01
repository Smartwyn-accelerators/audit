

import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({
    providedIn: 'root'
  })
export class ErrorService {
    constructor(private snackBar: MatSnackBar) {
    }

    showError(message: string, op = "", duration = 2000) {
        this.snackBar.open(message, op, {
            duration: duration,
        });
    }

}
