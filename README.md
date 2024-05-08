# Reuter-News-Data-Analysis


This project involves processing Reuters news data, performing sentiment analysis on news titles, and storing the results in a MongoDB database.

### Problem 1: Reuters News Data Reading & Transformation and Storing in MongoDB

**Objective**: To read news articles from provided files, extract relevant information, and store them in a MongoDB database.

1. **Data Processing**:
    - A Java program (ReutRead.java) to scan the required text between <TITLE></TITLE> and <BODY></BODY> tags within each <REUTER></REUTER> tag.
    - Extract the title and body of each news article.

2. **Storing in MongoDB**:
    - Creating a MongoDB database named ReuterDb.
    - Storing each news article as a document in the database.
    - Each document should contain fields for title and body, structured as follows:

```json
    {
  "title": "ADVANCED MAGNETICS ADMG IN AGREEMENT",
  "body": "Advanced Magnetics Inc said it reached a four mln dlrs research and development agreement with…"
    }
```

### Problem 2: Sentiment Analysis using Bag-of-Words Model on Reuters News Titles

**Objective**: Performing sentiment analysis on news article titles using a Bag-of-Words (BOW) model.

1. **Bag-of-Words Creation**: Implemented a Java program to create a bag-of-words for each news title. 

2. **Comparison with Positive and Negative Words**: 
    - Downloaded the lists of positive and negative words from online sources.
    - Compared each word in the bag-of-words with the lists of positive and negative words.
    - Performed word-by-word comparison to determine sentiment.

3. **Tagging News Titles**:
    - Tagged each news title as "positive", "negative", or "neutral" based on the overall score.
    - Inserted the titles and performed automatic matching with score detection using the program.
    - Structured the output in a tabular format for presentation.