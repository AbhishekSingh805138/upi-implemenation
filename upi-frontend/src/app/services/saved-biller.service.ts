import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { catchError, tap, retry } from 'rxjs/operators';
import { SavedBiller } from '../models/utility.model';

@Injectable({
  providedIn: 'root'
})
export class SavedBillerService {
  private apiUrl = 'http://localhost:8080/api/utilities/billers';
  private billersCache$ = new BehaviorSubject<SavedBiller[]>([]);

  constructor(private http: HttpClient) {}

  saveBiller(biller: SavedBiller): Observable<SavedBiller> {
    return this.http.post<SavedBiller>(this.apiUrl, biller)
      .pipe(
        tap(() => this.refreshCache(biller.userId)),
        catchError(this.handleError)
      );
  }

  getSavedBillers(userId: number): Observable<SavedBiller[]> {
    return this.http.get<SavedBiller[]>(`${this.apiUrl}/${userId}`)
      .pipe(
        retry(2),
        tap(billers => this.billersCache$.next(billers)),
        catchError(this.handleError)
      );
  }

  getBillersByCategory(userId: number, categoryName: string): Observable<SavedBiller[]> {
    return this.http.get<SavedBiller[]>(`${this.apiUrl}/${userId}/category/${categoryName}`)
      .pipe(
        retry(2),
        catchError(this.handleError)
      );
  }

  updateBiller(id: number, biller: SavedBiller): Observable<SavedBiller> {
    return this.http.put<SavedBiller>(`${this.apiUrl}/${id}`, biller)
      .pipe(
        tap(() => this.refreshCache(biller.userId)),
        catchError(this.handleError)
      );
  }

  deleteBiller(id: number, userId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`)
      .pipe(
        tap(() => this.refreshCache(userId)),
        catchError(this.handleError)
      );
  }

  getCachedBillers(): Observable<SavedBiller[]> {
    return this.billersCache$.asObservable();
  }

  private refreshCache(userId: number): void {
    this.getSavedBillers(userId).subscribe();
  }

  private handleError(error: any): Observable<never> {
    let errorMessage = 'An error occurred';
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Error: ${error.error.message}`;
    } else {
      errorMessage = error.error?.message || `Error Code: ${error.status}\nMessage: ${error.message}`;
    }
    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
