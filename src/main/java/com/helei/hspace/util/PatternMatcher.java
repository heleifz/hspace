package com.helei.hspace.util;

/**
 * multi pattern matcher
 */
public class PatternMatcher
{
    public static class MatchResult {
        private boolean success;
        /**
         * captured variables (type converted)
         */
        private Object[] captures;
        private int patternId;

        public MatchResult() {
            success = false;    
            captures = new Object[0];
            patternId = 0;
        }

		/**
		 * @return the success
		 */
		public boolean isSuccess() {
			return success;
		}

		/**
		 * @param success the success to set
		 */
		public void setSuccess(boolean success) {
			this.success = success;
		}

		/**
		 * @return the captures
		 */
		public Object[] getCaptures() {
			return captures;
		}

		/**
		 * @param captures the captures to set
		 */
		public void setCaptures(Object[] captures) {
			this.captures = captures;
		}

		/**
		 * @return the patternId
		 */
		public int getPatternId() {
			return patternId;
		}

		/**
		 * @param patternId the patternId to set
		 */
		public void setPatternId(int patternId) {
			this.patternId = patternId;
		}
    }

    public void register(String pattern, int id) {
    }

    public MatchResult match(String query) {
        return new MatchResult();
    }
}