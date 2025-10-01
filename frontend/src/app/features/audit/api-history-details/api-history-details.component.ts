import { Location } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

// Angular Material Modules
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

import { IAudit } from '../audit';

@Component({
  selector: 'app-api-history-details',
  templateUrl: './api-history-details.component.html',
  styleUrls: ['./api-history-details.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule
  ]
})
export class ApiHistoryDetailsComponent implements OnInit {
  apiHistoryDetails!: IAudit;
  constructor(private location: Location) {
  }

  ngOnInit() {
    this.apiHistoryDetails = this.location.getState() as IAudit;
    console.log(this.apiHistoryDetails);
  }

}
