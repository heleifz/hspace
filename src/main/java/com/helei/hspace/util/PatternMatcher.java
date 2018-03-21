package com.helei.hspace.util;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;

/**
 * multi pattern matcher
 */
public class PatternMatcher
{
    public static class Result {
        private boolean success;
        /**
         * captured variables
         */
        private String[] captures;
        private String patternId;

        public Result() {
            success = false;    
            captures = new String[0];
            patternId = "";
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
		public String[] getCaptures() {
			return captures;
		}

		/**
		 * @param captures the captures to set
		 */
		public void setCaptures(String[] captures) {
			this.captures = captures;
		}

		/**
		 * @return the patternId
		 */
		public String getPatternId() {
			return patternId;
		}

		/**
		 * @param patternId the patternId to set
		 */
		public void setPatternId(String patternId) {
			this.patternId = patternId;
		}
	}
	
	private ArrayList<Pair<Pattern, String>> patterns = new ArrayList<>();

	/**
	 * pattern is a regular expression with captures
	 */
    public void register(String pattern, String id) {
		Pattern compiled = Pattern.compile(pattern);
		patterns.add(new Pair<Pattern, String>(compiled, id));
    }

    public Result match(String query) {
		Result result = new Result();
		for (Pair<Pattern, String> pat : patterns) {
			Matcher mat = pat.getFirst().matcher(query);
			if (mat.matches()) {
				int matchCount = mat.groupCount();
				String[] captures = new String[matchCount];
				for (int i = 0; i < matchCount; ++i) {
					captures[i] = mat.group(i + 1);
				}
				result.setCaptures(captures);
				result.setPatternId(pat.getSecond());
				result.setSuccess(true);
				break;
			}
		}
        return result;
    }
}