# SchoolLifeSimulator

[Inspiration](http://www.polygon.com/2014/7/23/5926447/dwarf-fortress-will-crush-your-cpu-because-creating-history-is-hard)

### World state

**People**

- Name, birthday
- Happiness

**Locations**

- Classroom
- Bus stop
- LT

**Relations**

- Friendship (people)
- Connectedness (locations)
    
**Transitions**

Events: verb with params

- Move (someone from a to b)
- Talk/become friends (between 2 people)

**Flow**

Every iteration,

- Every actor has to pick an event to transition with

### Suggestions for how to implement certain things

- Attributes moving towards average (time): model with events
- Emotions like admiration: actually relations
