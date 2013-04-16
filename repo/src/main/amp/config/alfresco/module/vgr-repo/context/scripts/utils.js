/*
 * Pad a string.
 */
function pad(n) { 
	return n < 10 ? '0' + n : n;
}

/*
 * Convert a date to a correct Alfresco ISO date string.
 */
function isoDateString(d) {
	 return d.getUTCFullYear()+'-'
	      + pad(d.getUTCMonth()+1)+'-'
	      + pad(d.getUTCDate())+'T'
	      + pad(d.getUTCHours())+':'
	      + pad(d.getUTCMinutes())+':'
	      + pad(d.getUTCSeconds())+'.'
	      + pad(d.getUTCMilliseconds())+'Z'
}