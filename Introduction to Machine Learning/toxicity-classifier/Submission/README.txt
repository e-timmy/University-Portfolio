README 
Project Description: Effect of data-balancing techniques in toxicity classification tasks on performance and fairness.

The code for my implementation consists of a series of notebooks. The first is distinct from the rest
- A3_DataAnalysis: includes analysis of raw data, visualising class and group distributions, as well as sentiment analysis. 
The remaining are structured in four stages:
    1. Importing data: data drawn from subdirectory file called dataset, and wrapped in task-specific data class. Any resampling is applied here.
    2. Modeling: a wrapper class is applied to each model, to maintain state and allow unique predictions. A suite of models is applied using sklearn. This suite includes: KNN, Naive Bayes, Logistic-Regression, MLP.
    3. Evaluation: a wrapper class is applied to each model again, to maintain state and allow for different threshold adjustments and predictions. Two methods pertain are used to evaluate each class. First, publish() produces the performance results of each model. Second, fairness() produces the fairness results pertaining to 'Gender' as identity category of interest.
    4. Test Predictions: lastly, a wrapper is applied to exporting test predictions for each model. Each model is saved to a subdirectory labelled predictions, and made unique through their model descriptions (name and dataset)
The remaining datasets are:
    - A3_TfidfWork: Four stages applied to TFIDF model
    - A3_EWork: Four stages applied to Embedded model. For Threshold-Adjustment results, threshold line of code must be uncommented in evaluation section, and initial line of code commented.
    - A3_Synth: Four stages applied to Embedded model with SMOTE resampling

