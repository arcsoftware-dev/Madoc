import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-home',
  imports: [CommonModule],
  templateUrl: './home.html',
  styleUrls: ['./home.css'],
})
export class Home implements OnInit {
  newsArticles = [
    {
      title: 'League Expansion Announced',
      createdAt: new Date('2023-10-15'),
      summary: 'The MADOC Hockey League is excited to announce the addition of two new teams for the upcoming season.'
    },
    {
      title: 'New Stadium Opening',
      createdAt: new Date('2023-10-10'),
      summary: 'Our new state-of-the-art stadium will be opening next month with enhanced fan experience features.'
    }
  ];

  upcomingMatches = [
    {
      gameId: 1,
      startTime: new Date('2023-10-20T19:00:00'),
      homeTeam: 'MADOC Mavericks',
      awayTeam: 'Thunder Bay Thunder'
    },
    {
      gameId: 2,
      startTime: new Date('2023-10-21T19:30:00'),
      homeTeam: 'Saskatchewan Stars',
      awayTeam: 'Winnipeg Jets'
    }
  ];

  constructor(private router: Router) {}

  ngOnInit(): void {
    // Initialize any data needed for the component
  }

  stripSpaces(str: string): string {
    return str.replace(/\s+/g, '');
  }
}
