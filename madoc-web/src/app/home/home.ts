import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-home',
  imports: [CommonModule, RouterLink],
  templateUrl: './home.html',
  styleUrls: ['./home.css'],
})
export class Home implements OnInit {

  newsArticles = [
    {
      id: 1,
      title: 'League Expansion Announced',
      createdAt: new Date('2023-10-15'),
      summary: 'The MADOC Hockey League is excited to announce the addition of two new teams for the upcoming season.'
    },
    {
      id: 2,
      title: 'New Stadium Opening',
      createdAt: new Date('2023-10-10'),
      summary: 'Our new state-of-the-art stadium will be opening next month with enhanced fan experience features.'
    },
    {
      id: 3,
      title: 'New Stadium Opening',
      createdAt: new Date('2023-10-10'),
      summary: 'Our new state-of-the-art stadium will be opening next month with enhanced fan experience features.'
    },
    {
      id: 4,
      title: 'New Stadium Opening',
      createdAt: new Date('2023-10-10'),
      summary: 'Our new state-of-the-art stadium will be opening next month with enhanced fan experience features.'
    }
  ];

  upcomingMatches = [
    {
      id: 1,
      homeTeam: 'Leafs',
      awayTeam: 'Canucks',
      date: new Date('2023-11-01T19:00:00'),
      location: 'Madoc Arena'
    },
    {
      id: 2,
      homeTeam: 'Red Wings',
      awayTeam: 'Golden Knights',
      date: new Date('2023-11-05T20:00:00'),
      location: 'Trenton Sports Complex'
    },
    {
      id: 3,
      homeTeam: 'Avalanche',
      awayTeam: 'Blackhawks',
      date: new Date('2023-11-10T18:30:00'),
      location: 'Cobourg Ice Rink'
    }
  ]

  constructor(private router: Router) {}

  ngOnInit(): void {
    // Initialize any data needed for the component
  }

  stripSpaces(str: string): string {
    return str.replace(/\s+/g, '');
  }
}
