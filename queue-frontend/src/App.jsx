
import { useState, useEffect, useCallback } from 'react'
import QRCode from 'qrcode'
import './App.css'

const API = 'http://10.203.20.27:8080/api/queue'
const CUSTOMER_URL = 'http://10.203.20.27:5173'
const PAGE_SIZE = 10

function App() {
  const [queue, setQueue] = useState([])
  const [error, setError] = useState('')
  const [lastUpdated, setLastUpdated] = useState(null)

  const [qrCodeUrl, setQrCodeUrl] = useState('')
  const [qrDate, setQrDate] = useState('')
  const [qrError, setQrError] = useState('')
  const [generatingQr, setGeneratingQr] = useState(false)

  // Independent page numbers for both tables
  const [activePage, setActivePage] = useState(1)
  const [completedPage, setCompletedPage] = useState(1)

  const getQueue = useCallback(async () => {
    try {
      const response = await fetch(API)

      if (!response.ok) {
        throw new Error('Could not load queue')
      }

      const data = await response.json()

      setQueue(data)
      setError('')
      setLastUpdated(new Date())
    } catch {
      setError(
        'Unable to connect. Check that Spring Boot is running.'
      )
    }
  }, [])

  useEffect(() => {
    getQueue()

    const interval = setInterval(getQueue, 5000)

    return () => clearInterval(interval)
  }, [getQueue])

  const generateDailyQr = async () => {
    setGeneratingQr(true)
    setQrError('')

    try {
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      const today = `${year}-${month}-${day}`

      const registrationUrl = `${CUSTOMER_URL}?date=${today}`

      const imageUrl = await QRCode.toDataURL(registrationUrl, {
        width: 260,
        margin: 2,
        errorCorrectionLevel: 'M',
      })

      setQrCodeUrl(imageUrl)
      setQrDate(today)
    } catch {
      setQrError(
        'Could not generate QR code. Please try again.'
      )
    } finally {
      setGeneratingQr(false)
    }
  }

  const performAction = async (id, action, label) => {
    try {
      const response = await fetch(
        `${API}/${id}/${action}`,
        { method: 'PUT' }
      )

      if (!response.ok) {
        throw new Error(`${label} failed`)
      }

      await getQueue()
    } catch (err) {
      alert(err.message || `Unable to ${label.toLowerCase()}.`)
    }
  }

  // Separate customers by status
  const active = queue.filter(
    (customer) => customer.status !== 'COMPLETED'
  )

  const waiting = queue.filter(
    (customer) => customer.status === 'WAITING'
  )

  const serving = queue.filter(
    (customer) => customer.status === 'SERVING'
  )

  const completed = queue.filter(
    (customer) => customer.status === 'COMPLETED'
  )

  const called = queue.filter(
    (customer) => customer.status === 'CALLED'
  )

  const late = queue.filter(
    (customer) => customer.status === 'LATE'
  )

  // Calculate total pages for each table
  const activeTotalPages = Math.max(
    1,
    Math.ceil(active.length / PAGE_SIZE)
  )

  const completedTotalPages = Math.max(
    1,
    Math.ceil(completed.length / PAGE_SIZE)
  )

  // Keep page numbers valid when the queue changes
  const safeActivePage = Math.min(
    activePage,
    activeTotalPages
  )

  const safeCompletedPage = Math.min(
    completedPage,
    completedTotalPages
  )

  // Get only the customers for the current page
  const activeStart = (safeActivePage - 1) * PAGE_SIZE
  const completedStart = (safeCompletedPage - 1) * PAGE_SIZE

  const activePageCustomers = active.slice(
    activeStart,
    activeStart + PAGE_SIZE
  )

  const completedPageCustomers = completed.slice(
    completedStart,
    completedStart + PAGE_SIZE
  )

  // Reusable pagination controls
  const Pagination = ({
    currentPage,
    totalPages,
    onPageChange,
    totalItems,
  }) => {
    const startItem =
      totalItems === 0 ? 0 : (currentPage - 1) * PAGE_SIZE + 1

    const endItem = Math.min(
      currentPage * PAGE_SIZE,
      totalItems
    )

    return (
      <div className="pagination">
        <span className="pagination-info">
          Showing {startItem}–{endItem} of {totalItems}
        </span>

        <div className="pagination-controls">
          <button
            type="button"
            className="pagination-button"
            disabled={currentPage <= 1}
            onClick={() => onPageChange(currentPage - 1)}
          >
            Previous
          </button>

          <span className="pagination-page">
            Page {currentPage} of {totalPages}
          </span>

          <button
            type="button"
            className="pagination-button"
            disabled={currentPage >= totalPages}
            onClick={() => onPageChange(currentPage + 1)}
          >
            Next
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="dashboard">
      <header className="topbar">
        <div className="brand">
          <div className="brand-icon">AQ</div>
          <div>
            <h1>QueueFlow</h1>
            <p>Adaptive Queue Management</p>
          </div>
        </div>

        <div className="staff-profile">
          <div className="online-dot"></div>
          <span>Office Staff</span>
        </div>
      </header>

      <main className="main-content">
        <section className="welcome-section">
          <div>
            <p className="eyebrow">OFFICE MANAGEMENT</p>
            <h2>Staff Dashboard</h2>
            <p className="welcome-text">
              Manage customers and monitor queue waiting times.
            </p>
          </div>

          <button className="refresh-button" onClick={getQueue}>
            ↻ Refresh Queue
          </button>
        </section>

        {error && (
          <div className="error-message" role="alert">
            {error}
          </div>
        )}

        {/* Daily registration QR */}
        <section className="queue-panel">
          <div className="panel-heading">
            <div>
              <h2>Customer Registration QR</h2>
              <p>Generate today's QR code for customers to scan.</p>
            </div>
          </div>

          <div style={{ padding: '24px', textAlign: 'center' }}>
            <button
              className="refresh-button"
              onClick={generateDailyQr}
              disabled={generatingQr}
            >
              {generatingQr
                ? 'Generating...'
                : "Generate Today's QR Code"}
            </button>

            {qrError && (
              <p className="error-message" role="alert">
                {qrError}
              </p>
            )}

            {qrCodeUrl && (
              <div style={{ marginTop: '20px' }}>
                <h3>Today's Registration QR</h3>
                <p>Date: {qrDate}</p>

                <img
                  src={qrCodeUrl}
                  alt="Today's customer registration QR code"
                  width="260"
                  height="260"
                  style={{
                    display: 'block',
                    margin: '16px auto',
                    maxWidth: '100%',
                  }}
                />

                <p>
                  Ask customers to scan this code to open
                  the registration page.
                </p>

                <a
                  href={qrCodeUrl}
                  download={`queue-qr-${qrDate}.png`}
                >
                  Download QR Code
                </a>
              </div>
            )}
          </div>
        </section>

        {/* Summary cards */}
        <section className="summary-grid">
          <article className="summary-card blue-card">
            <div className="summary-top">
              <span className="summary-label">Active Customers</span>
              <span className="summary-icon">👥</span>
            </div>
            <p className="summary-number">{active.length}</p>
            <span className="summary-note">Customers in progress</span>
          </article>

          <article className="summary-card orange-card">
            <div className="summary-top">
              <span className="summary-label">Waiting</span>
              <span className="summary-icon">⏳</span>
            </div>
            <p className="summary-number">{waiting.length}</p>
            <span className="summary-note">Waiting for their turn</span>
          </article>

          <article className="summary-card green-card">
            <div className="summary-top">
              <span className="summary-label">Currently Serving</span>
              <span className="summary-icon">✓</span>
            </div>
            <p className="summary-number">{serving.length}</p>
            <span className="summary-note">Service in progress</span>
          </article>

          <article className="summary-card purple-card">
            <div className="summary-top">
              <span className="summary-label">Completed</span>
              <span className="summary-icon">🏁</span>
            </div>
            <p className="summary-number">{completed.length}</p>
            <span className="summary-note">Service finished</span>
          </article>
        </section>

        {/* Current queue with pagination */}
        <section className="queue-panel">
          <div className="panel-heading">
            <div>
              <h2>Current Queue</h2>
              <p>Manage active customer requests.</p>
            </div>

            <span className="live-badge">
              <span className="live-dot"></span>
              Live Updates
            </span>
          </div>

          <div className="queue-filters">
            <span>All Active <b>{active.length}</b></span>
            <span>Called <b>{called.length}</b></span>
            <span>Late <b>{late.length}</b></span>
          </div>

          <div className="table-wrapper">
            <table className="queue-table">
              <thead>
                <tr>
                  <th>Token</th>
                  <th>Customer</th>
                  <th>Status</th>
                  <th>Position</th>
                  <th>Estimated Wait</th>
                  <th>Staff Actions</th>
                </tr>
              </thead>

              <tbody>
                {activePageCustomers.map((customer) => (
                  <tr key={customer.id}>
                    <td>
                      <span className="token-badge">
                        #{customer.tokenNumber}
                      </span>
                    </td>

                    <td>
                      <div className="customer-cell">
                        <div className="customer-avatar">
                          {(customer.customerName || '?')
                            .charAt(0)
                            .toUpperCase()}
                        </div>
                        <span>
                          {customer.customerName || 'Name missing'}
                        </span>
                      </div>
                    </td>

                    <td>
                      <span
                        className={`status-badge status-${customer.status}`}
                      >
                        {customer.status}
                      </span>
                    </td>

                    <td>
                      <span className="position-value">
                        {customer.queuePosition ?? '—'}
                      </span>
                    </td>

                    <td>
                      <span className="wait-time">
                        {customer.estimatedWaitingTime ||
                          'Calculating...'}
                      </span>
                    </td>

                    <td>
                      <div className="action-buttons">
                        {customer.status === 'WAITING' && (
                          <button
                            className="action-button call-button"
                            onClick={() =>
                              performAction(customer.id, 'call', 'Call customer')
                            }
                          >
                            Call
                          </button>
                        )}

                        {customer.status === 'CALLED' && (
                          <>
                            <button
                              className="action-button start-button"
                              onClick={() =>
                                performAction(
                                  customer.id,
                                  'start',
                                  'Start service'
                                )
                              }
                            >
                              Start Service
                            </button>

                            <button
                              className="action-button late-button"
                              onClick={() =>
                                performAction(
                                  customer.id,
                                  'late',
                                  'Mark late'
                                )
                              }
                            >
                              Late
                            </button>
                          </>
                        )}

                        {customer.status === 'LATE' && (
                          <button
                            className="action-button arrive-button"
                            onClick={() =>
                              performAction(
                                customer.id,
                                'arrive',
                                'Record arrival'
                              )
                            }
                          >
                            Arrived
                          </button>
                        )}

                        {customer.status === 'SERVING' && (
                          <button
                            className="action-button complete-button"
                            onClick={() =>
                              performAction(
                                customer.id,
                                'complete',
                                'Complete service'
                              )
                            }
                          >
                            Complete
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}

                {active.length === 0 && (
                  <tr>
                    <td colSpan="6" className="empty-state">
                      <span>🎉</span>
                      <p>No active customers in the queue.</p>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>

          <Pagination
            currentPage={safeActivePage}
            totalPages={activeTotalPages}
            totalItems={active.length}
            onPageChange={setActivePage}
          />

          <div className="panel-footer">
            <span>{active.length} active customer(s)</span>
            <span>
              {lastUpdated
                ? `Updated at ${lastUpdated.toLocaleTimeString()}`
                : 'Loading queue...'}
            </span>
          </div>
        </section>

        {/* Completed customers with independent pagination */}
        <section className="queue-panel completed-panel">
          <div className="panel-heading">
            <div>
              <h2>Completed Customers</h2>
              <p>Customers who have finished service.</p>
            </div>

            <span className="completed-count">
              {completed.length} completed
            </span>
          </div>

          <div className="table-wrapper">
            <table className="queue-table">
              <thead>
                <tr>
                  <th>Token</th>
                  <th>Customer</th>
                  <th>Status</th>
                </tr>
              </thead>

              <tbody>
                {completedPageCustomers.map((customer) => (
                  <tr key={customer.id}>
                    <td>
                      <span className="token-badge">
                        #{customer.tokenNumber}
                      </span>
                    </td>

                    <td>
                      {customer.customerName || 'Name missing'}
                    </td>

                    <td>
                      <span className="status-badge status-COMPLETED">
                        COMPLETED
                      </span>
                    </td>
                  </tr>
                ))}

                {completed.length === 0 && (
                  <tr>
                    <td colSpan="3" className="empty-state">
                      No completed customers yet.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>

          <Pagination
            currentPage={safeCompletedPage}
            totalPages={completedTotalPages}
            totalItems={completed.length}
            onPageChange={setCompletedPage}
          />
        </section>

        <footer className="dashboard-footer">
          Adaptive Queue Management System
          <span>•</span>
          Staff Dashboard
        </footer>
      </main>
    </div>
  )
}

export default App
