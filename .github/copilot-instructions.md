By default, when user asked to edit a file, make sure to:

- Read the related files, for example, if the user asks to edit an Activity, read the related layout
  file; or if the user asks to edit a variable, make sure to read and edit corresponding DAOs,
  entities, ...
- Don't write obvious comments, like "This is a variable for ...", or "This is a function to ...".
  Instead, write comments that explain why the code is written in a certain way, or what the
  implications of the code are.
- If the user asks to add a new feature, make sure to read the related files and understand the
  context of the feature. Don't just add the feature without understanding how it fits into the
  existing codebase.
- Always search for existing code in this order:
    1. The current file
    2. The files imported in the current file
    3. The files in current package
    4. Global files, such as utilities or notes.

- Data flow: UI > Model > DAO > Entity > Database