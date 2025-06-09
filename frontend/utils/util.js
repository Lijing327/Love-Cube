const formatTime = date => {
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hour = date.getHours()
  const minute = date.getMinutes()
  const second = date.getSeconds()

  return `${[year, month, day].map(formatNumber).join('/')} ${[hour, minute, second].map(formatNumber).join(':')}`
}

const formatNumber = n => {
  n = n.toString()
  return n[1] ? n : `0${n}`
}

const formatDate = (date, format = 'YYYY-MM-DD') => {
  const d = new Date(date)
  const year = d.getFullYear()
  const month = formatNumber(d.getMonth() + 1)
  const day = formatNumber(d.getDate())
  
  if (format === 'YYYY-MM-DD') {
    return `${year}-${month}-${day}`
  } else if (format === 'MM-DD') {
    return `${month}-${day}`
  }
  return `${year}-${month}-${day}`
}

const formatDistance = meters => {
  if (meters < 1000) {
    return meters + 'm'
  }
  return (meters / 1000).toFixed(1) + 'km'
}

const formatAge = birthday => {
  const today = new Date()
  const birthDate = new Date(birthday)
  let age = today.getFullYear() - birthDate.getFullYear()
  const m = today.getMonth() - birthDate.getMonth()
  if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) {
    age--
  }
  return age
}

const debounce = (fn, delay) => {
  let timer = null
  return function (...args) {
    if (timer) {
      clearTimeout(timer)
    }
    timer = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}

const throttle = (fn, delay) => {
  let timer = null
  let start = Date.now()
  return function (...args) {
    const current = Date.now()
    const remaining = delay - (current - start)
    clearTimeout(timer)
    if (remaining <= 0) {
      fn.apply(this, args)
      start = Date.now()
    } else {
      timer = setTimeout(() => {
        fn.apply(this, args)
        start = Date.now()
      }, remaining)
    }
  }
}

module.exports = {
  formatTime,
  formatDate,
  formatDistance,
  formatAge,
  debounce,
  throttle
} 