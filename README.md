# MedEnhance

Created for MHacks 2015

## Inspiration
We decided to make this app because while online medical websites such as WebMD may give information about the symptoms of the disease, they do not give information about the probability you may have a certain disease given a specific set of symptoms. We decided to make this app to calculate that probability for users.

## How it works
The app collects data from its online database (Microsoft Azure based), and from there calculates the probability of each symptom in the total population (the database). It also calculates the probability of the disease occurring. Then, using some more statistical calculations, we can come up with a percentage representing the probability of the disease given that set of symptoms.

## Challenges I ran into
Initially, the application was going to be preloaded with data collected from online about the probability of individual symptoms. However, we found this information to be difficult, if not impossible, to find, so we switched to a user data driven model. Users can submit what diseases they have had in the past, and what symptoms they encountered. As the user base grows, our database of disease submissions will more and more closely resemble the general population.

## Accomplishments that I'm proud of
The user data driven model is one of our proudest accomplishments because it allows the app to constantly improve the quality of its predictions and raises the ceiling on what the app can accomplish.

## What I learned
We learned about using Microsoft Azure and database technologies such as MySQL.

## What's next for MedEnhance
We hope to make the app more robust, as currently there is no way to determine whether a submission is accurate or not. We believe this application could be extremely useful in a hospital setting, allowing doctors and nurses to quickly log patient data. This would build an accurate and large user base, which would give them accurate predictions about patient illnesses.
